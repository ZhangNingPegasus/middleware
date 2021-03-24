package org.wyyt.sharding.db2es.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.elasticsearch.service.ElasticSearchService;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.db2es.admin.config.PropertyConfig;
import org.wyyt.sharding.db2es.admin.service.TopicDb2EsService;
import org.wyyt.sharding.db2es.admin.service.TopicService;
import org.wyyt.sharding.db2es.admin.service.common.ShardingDbService;
import org.wyyt.sharding.db2es.admin.utils.EsMappingUtils;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The controller for params' pages.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(TopicController.PREFIX)
public class TopicController {
    public static final String PREFIX = "topic";
    private final TopicDb2EsService topicDb2EsService;
    private final ShardingService shardingService;
    private final TopicService topicService;
    private final ShardingDbService shardingDbService;
    private final PropertyConfig propertyConfig;
    private final ElasticSearchService elasticSearchService;

    public TopicController(final TopicDb2EsService topicDb2EsService,
                           final ShardingService shardingService,
                           final TopicService topicService,
                           final ShardingDbService shardingDbService,
                           final PropertyConfig propertyConfig,
                           final ElasticSearchService elasticSearchService) {
        this.topicDb2EsService = topicDb2EsService;
        this.shardingService = shardingService;
        this.topicService = topicService;
        this.shardingDbService = shardingDbService;
        this.propertyConfig = propertyConfig;
        this.elasticSearchService = elasticSearchService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        final List<String> tables = this.shardingService.listTableProperties().stream().map(TableProperty::getName).collect(Collectors.toList());
        final List<String> topicList = this.topicService.list().stream().map(Topic::getName).collect(Collectors.toList());
        tables.removeAll(topicList);

        int replicaNum = this.propertyConfig.getEsHost().split(",").length - 1;
        model.addAttribute("replicaNum", replicaNum);
        model.addAttribute("tables", tables);
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("todetail")
    public String toDetail(final Model model,
                           @RequestParam(value = "id") final Long id) {
        final Topic topic = this.topicService.getById(id);
        model.addAttribute("topic", topic);
        return String.format("%s/%s", PREFIX, "detail");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Topic>> list(@RequestParam(value = "name", required = false) final String name) {
        return Result.ok(this.topicService.listTopic(name));
    }

    @PostMapping("getSource")
    @ResponseBody
    public Result<String> getSource(@RequestParam(value = "tableName") final String tableName) throws Exception {
        final List<TableProperty> tableProperties = this.shardingService.listTableByName(tableName);
        if (null == tableProperties || tableProperties.isEmpty()) {
            return Result.error(String.format("不存在表[%s]", tableName));
        }

        final Map<String, TableProperty.DimensionInfo> dimensionInfos = tableProperties.get(0).getDimensionInfos();
        final Map.Entry<String, TableProperty.DimensionInfo> dimensionInfo = dimensionInfos.entrySet().iterator().next();
        final Map<String, DataSourceProperty> dataSourceProperties = dimensionInfo.getValue().getDimensionProperty().getDataSourceProperties();
        final List<String> dbNameList = new ArrayList<>(dataSourceProperties.keySet());
        dbNameList.sort(String::compareTo);

        final DataSource dataSource = this.shardingDbService.getDataSourceByName(dbNameList.get(0));
        final List<FieldInfo> fieldInfoList = this.shardingService.listFields(dataSource, String.format(dimensionInfo.getValue().getTableNameFormat(), 0));
        return Result.ok(Utils.toPrettyFormatJson(EsMappingUtils.getEsSource(
                this.elasticSearchService.getNodesCount(),
                this.elasticSearchService.getNodesCount() - 1,
                fieldInfoList)));
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name") final String name,
                         @RequestParam(value = "aliasOfYears") final Integer aliasOfYears,
                         @RequestParam(value = "source") final String source,
                         @RequestParam(value = "description") final String description) {
        if(!CommonTool.isJson(source)){
            return Result.error("ES的source不是标准的JSON格式");
        }
        Topic topic = this.topicService.getByName(name);
        if (null != topic) {
            return Result.error(String.format("主题[%s]已存在", name));
        }
        topic = new Topic();
        topic.setName(name);
        topic.setAliasOfYears(aliasOfYears);
        topic.setSource(source);
        topic.setDescription(description);
        this.topicService.save(topic);
        return Result.ok();
    }

    @PostMapping("delete")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "id") final Long id) {
        this.topicDb2EsService.deleteTopic(id);
        return Result.ok();
    }
}