package org.wyyt.db2es.admin.controller;

import com.alibaba.fastjson.JSON;
import com.sijibao.nacos.spring.util.NacosRsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.common.Utils;
import org.wyyt.db2es.admin.entity.vo.DataSourceVo;
import org.wyyt.db2es.admin.entity.vo.RebuildVo;
import org.wyyt.db2es.admin.rebuild.RebuildService;
import org.wyyt.db2es.admin.service.TopicService;
import org.wyyt.db2es.admin.service.common.ShardingDbService;
import org.wyyt.db2es.admin.utils.EsMappingUtils;
import org.wyyt.db2es.core.entity.persistent.Topic;
import org.wyyt.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.web.Result;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;


/**
 * The controller for rebuild.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(RebuildController.PREFIX)
public class RebuildController {
    public static final String PREFIX = "rebuild";
    private final ShardingService shardingService;
    private final ShardingDbService shardingDbService;
    private final TopicService topicService;
    private final RebuildService rebuildService;

    public RebuildController(final ShardingService shardingService,
                             final ShardingDbService shardingDbService,
                             final TopicService topicService,
                             final RebuildService rebuildService) {
        this.shardingService = shardingService;
        this.shardingDbService = shardingDbService;
        this.topicService = topicService;
        this.rebuildService = rebuildService;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        model.addAttribute("inRebuild", this.rebuildService.isRunning());
        model.addAttribute("topic", this.rebuildService.getTopic());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("torebuild")
    public String toRebuild(final Model model,
                            @RequestParam("name") final String topicName) throws Exception {
        final Topic topic = this.topicService.getByName(topicName);
        final TableProperty tableProperty = this.shardingService.getTableProperty(topicName);
        final DimensionProperty dimensionProperty = this.shardingService.getPrimaryDimensionProperty(topicName);
        final List<DataSourceVo> dataSourceVoList = this.getAcmDataSource(topicName);

        model.addAttribute("datasources", dataSourceVoList);
        model.addAttribute("topic", topic);
        model.addAttribute("topicName", topicName);

        if (null == topic) {
            final List<TableProperty> tableProperties = this.shardingService.listTableByName(topicName);
            if (null == tableProperties || tableProperties.isEmpty()) {
                throw new Db2EsException(String.format("不存在表[%s]", topicName));
            }
            final Map<String, TableProperty.DimensionInfo> dimensionInfos = tableProperties.get(0).getDimensionInfos();
            final Map.Entry<String, TableProperty.DimensionInfo> dimensionInfo = dimensionInfos.entrySet().iterator().next();
            final Map<String, DataSourceProperty> dataSourceProperties = dimensionInfo.getValue().getDimensionProperty().getDataSourceProperties();
            final List<String> dbNameList = new ArrayList<>(dataSourceProperties.keySet());
            dbNameList.sort(String::compareTo);

            final DataSource dataSource = this.shardingDbService.getDataSourceByName(dbNameList.get(0));
            final List<FieldInfo> fieldVos = this.shardingService.listFields(dataSource, String.format(dimensionInfo.getValue().getTableNameFormat(), 0));
            model.addAttribute("mapping", Utils.toPrettyFormatJson(EsMappingUtils.getEsMapping(fieldVos)));
        } else {
            model.addAttribute("mapping", Utils.toPrettyFormatJson(topic.getMapping()));
        }
        return String.format("%s/%s", PREFIX, "rebuild");
    }

    @GetMapping("toRebuildDetail")
    public String toRebuildDetail() {
        return String.format("%s/%s", PREFIX, "rebuilddetail");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<RebuildVo>> list(@RequestParam(value = "indexName", required = false) final String indexName) {
        final List<TableProperty> tableProperties = this.shardingService.listTableProperties();
        final List<RebuildVo> rebuildVoList = new ArrayList<>();
        boolean disableAll = false;
        for (final TableProperty tableProperty : tableProperties) {
            if (!StringUtils.isEmpty(indexName) && !tableProperty.getName().equals(indexName)) {
                continue;
            }
            final RebuildVo rebuildVo = new RebuildVo();
            BeanUtils.copyProperties(tableProperty, rebuildVo);
            rebuildVo.setInRebuild(false);
            rebuildVo.setDisableRebuild(false);
            if (null != this.rebuildService.getTopic() &&
                    tableProperty.getName().equalsIgnoreCase(this.rebuildService.getTopic().getName())) {
                if (this.rebuildService.isRunning()) {
                    rebuildVo.setInRebuild(true);
                    disableAll = true;
                }
            }
            rebuildVoList.add(rebuildVo);
        }

        if (disableAll) {
            for (final RebuildVo rebuildVo : rebuildVoList) {
                rebuildVo.setDisableRebuild(true);
            }
        }
        final Map<String, Topic> topicMap = this.topicService.listTopicMap(null);
        for (final RebuildVo rebuildVo : rebuildVoList) {
            final Topic topic = topicMap.get(rebuildVo.getName());
            if (null != topic) {
                rebuildVo.setNumberOfShards(topic.getNumberOfShards());
                rebuildVo.setNumberOfReplicas(topic.getNumberOfReplicas());
                rebuildVo.setRefreshInterval(topic.getRefreshInterval());
                rebuildVo.setAliasOfYears(topic.getAliasOfYears());
                rebuildVo.setDescription(topic.getDescription());
                if (!topic.getRowCreateTime().equals(topic.getRowUpdateTime())) {
                    rebuildVo.setRowUpdateTime(topic.getRowUpdateTime());
                }
            }
        }
        return Result.success(rebuildVoList);
    }

    @PostMapping("rebuild")
    @ResponseBody
    public Result<?> rebuild(final Topic topic,
                             @RequestParam(value = "datasources") final String datasources,
                             @RequestParam(value = "type") final Integer type) throws Exception {
        List<DataSourceVo> dataSourceVoList;
        final Map<DataSourceVo, Set<String>> tableSourceMap = new HashMap<>();

        if (type.equals(0)) {
            dataSourceVoList = new ArrayList<>(getAcmDataSource(topic.getName()));
        } else {
            dataSourceVoList = JSON.parseArray(datasources, DataSourceVo.class);
        }

        for (final DataSourceVo dataSourceVo : dataSourceVoList) {
            final Set<String> tableNameSet = new HashSet<>();
            final String[] tableNames = dataSourceVo.getTableNames().split(",");
            for (final String tableName : tableNames) {
                if (null == tableName || StringUtils.isEmpty(tableName.trim())) {
                    continue;
                }
                tableNameSet.add(tableName.trim());
            }
            if (tableSourceMap.containsKey(dataSourceVo)) {
                throw new Db2EsException(String.format("检测到重复的数据源信息: [%s], 数据源的配置信息不能重复", dataSourceVo.getDatabaseName()));
            }
            tableSourceMap.put(dataSourceVo, tableNameSet);
        }
        this.rebuildService.rebuild(topic, tableSourceMap);
        return Result.success();
    }

    @PostMapping("stopRebuild")
    @ResponseBody
    public Result<?> stopRebuild() throws Exception {
        this.rebuildService.stopRebuild();
        return Result.success();
    }

    @PostMapping("rebuilddetail")
    @ResponseBody
    public Result<RebuildService.RebuildStatus> rebuildDetail() {
        return Result.success(this.rebuildService.getRebuildStatus());
    }

    @PostMapping("clearRebuild")
    @ResponseBody
    public Result<?> clearRebuild() throws InterruptedException {
        this.rebuildService.clearRebuild();
        return Result.success();
    }

    @PostMapping("test")
    @ResponseBody
    public Result<?> test(@RequestParam(value = "host") final String host,
                          @RequestParam(value = "port") final String port,
                          @RequestParam(value = "uid") final String uid,
                          @RequestParam(value = "pwd") final String pwd,
                          @RequestParam(value = "databaseName") final String databaseName,
                          @RequestParam(value = "tableNames") final String tableNames) throws Exception {
        final String[] all = tableNames.split(",");
        final Set<String> tableNameSet = new HashSet<>(all.length);
        for (final String tableName : all) {
            if (null == tableName || StringUtils.isEmpty(tableName.trim())) {
                continue;
            }
            tableNameSet.add(tableName);
        }
        DataSource dataSource = null;
        Connection connection = null;
        try {
            dataSource = DataSourceTool.createHikariDataSource(host, port, databaseName, uid, pwd, 1, 1);
            connection = dataSource.getConnection();
            for (final String tableName : tableNameSet) {
                final PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM `%s` LIMIT 0", tableName));
                preparedStatement.execute();
                DataSourceTool.close(preparedStatement);
            }
        } finally {
            DataSourceTool.close(connection);
            DataSourceTool.close(dataSource);
        }
        return Result.success();
    }

    private List<DataSourceVo> getAcmDataSource(final String logicTableName) {
        final List<DataSourceVo> dataSourceVoList = new ArrayList<>();
        final DimensionProperty dimensionProperty = this.shardingService.getPrimaryDimensionProperty(logicTableName);
        for (final DataSourceProperty value : dimensionProperty.getDataSourceProperties().values()) {
            final DataSourceVo dataSourceVo = new DataSourceVo();
            dataSourceVo.setHost(value.getHost());
            dataSourceVo.setPort(value.getPort());
            dataSourceVo.setUid(value.getUsername());
            dataSourceVo.setPwd(NacosRsaUtils.decrypt(value.getPassword()));
            dataSourceVo.setDatabaseName(value.getDatabaseName());
            dataSourceVoList.add(dataSourceVo);

            final TableProperty.DimensionInfo tableDimensionInfo = this.shardingService.getTableDimensionInfo(logicTableName, dimensionProperty.getName());
            final int dbCount = dimensionProperty.getDataSourceProperties().size();
            final int lastIndexOf = value.getDatabaseName().lastIndexOf("_");
            final int countPerDb = tableDimensionInfo.getTableCountNum() / dbCount;
            final int start = value.getIndex() * countPerDb;
            final int end = start + countPerDb - 1;
            final List<String> tableList = new ArrayList<>(countPerDb);
            for (int i = start; i <= end; i++) {
                tableList.add(String.format(tableDimensionInfo.getTableNameFormat(), i));
            }
            dataSourceVo.setTableNames(StringUtils.join(tableList.toArray(new String[]{}), ','));
        }
        dataSourceVoList.sort(Comparator.comparing(DataSourceVo::getDatabaseName));
        return dataSourceVoList;
    }
}