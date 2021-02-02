package org.wyyt.sharding.db2es.admin.controller;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sharding.db2es.admin.entity.vo.CompareJsonVo;
import org.wyyt.sharding.db2es.admin.entity.vo.TopicDb2EsVo;
import org.wyyt.sharding.db2es.admin.entity.vo.TopicInfoVo;
import org.wyyt.sharding.db2es.admin.service.RepairService;
import org.wyyt.sharding.db2es.admin.service.TopicDb2EsService;
import org.wyyt.sharding.db2es.admin.service.common.Db2EsHttpService;
import org.wyyt.sharding.db2es.admin.service.common.EsService;
import org.wyyt.sharding.db2es.admin.service.common.ShardingDbService;
import org.wyyt.sharding.db2es.admin.utils.CompareUtils;
import org.wyyt.sharding.db2es.core.entity.domain.IndexName;
import org.wyyt.sharding.db2es.core.entity.domain.IndexSetting;
import org.wyyt.sharding.db2es.core.entity.domain.TopicOffset;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.entity.view.IndexVo;
import org.wyyt.sharding.db2es.core.entity.view.NodeVo;
import org.wyyt.sharding.db2es.core.entity.view.SettingVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.entity.ShardingResult;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.tool.rpc.Result;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.wyyt.sharding.db2es.admin.controller.ServerController.PREFIX;

/**
 * The controller of server page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ServerController {
    public static final String PREFIX = "server";
    private final Db2EsHttpService db2EsHttpService;
    private final EsService esService;
    private final TopicDb2EsService topicDb2EsService;
    private final ShardingService shardingService;
    private final ShardingDbService shardingDbService;
    private final RepairService repairService;

    public ServerController(final Db2EsHttpService db2EsHttpService,
                            final EsService esService,
                            final TopicDb2EsService topicDb2EsService,
                            final ShardingService shardingService,
                            final ShardingDbService shardingDbService,
                            final RepairService repairService) {
        this.db2EsHttpService = db2EsHttpService;
        this.esService = esService;
        this.topicDb2EsService = topicDb2EsService;
        this.shardingService = shardingService;
        this.shardingDbService = shardingDbService;
        this.repairService = repairService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }


    @GetMapping("toadd")
    public String toadd(final Model model) throws Exception {
        model.addAttribute("db2esList", this.db2EsHttpService.listDd2Es());
        model.addAttribute("topicList", this.topicDb2EsService.listUnused());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toreset")
    public String toReset(final Model model,
                          @RequestParam(value = "topicName") final String topicName,
                          @RequestParam(value = "offset") final String offset,
                          @RequestParam(value = "timestamp") final String timestamp) {
        model.addAttribute("topicName", topicName);

        if ("null".equalsIgnoreCase(timestamp)) {
            model.addAttribute("offset", "暂无偏移量");
            model.addAttribute("timestamp", "暂无提交时间");
        } else {
            model.addAttribute("offset", offset);
            model.addAttribute("timestamp", timestamp);
        }

        return String.format("%s/%s", PREFIX, "reset");
    }

    @GetMapping("todetail")
    public String toDetail(final Model model,
                           @RequestParam(value = "indexName") final String indexName) {
        model.addAttribute("shardingColumn", this.shardingService.listShardingColumns(indexName).iterator().next());
        model.addAttribute("indexName", indexName);
        return String.format("%s/%s", PREFIX, "detail");
    }

    @GetMapping("tosetting")
    public String toSetting(final Model model,
                            @RequestParam(value = "topicName") final String topicName) {
        model.addAttribute("topicName", topicName);
        return String.format("%s/%s", PREFIX, "setting");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<TopicInfoVo>> list(@RequestParam(value = "topicName", required = false) final String topicName) throws Exception {
        final List<TopicInfoVo> topicInfoVoList = this.db2EsHttpService.getTopicVoList(topicName);
        final Map<String, Long> countMap = this.esService.listCount(topicInfoVoList.stream().map(TopicInfoVo::getTopicName).collect(Collectors.toSet()));
        for (final TopicInfoVo topicInfoVo : topicInfoVoList) {
            if (countMap.containsKey(topicInfoVo.getTopicName())) {
                topicInfoVo.setDocsCount(countMap.get(topicInfoVo.getTopicName()));
            }

            final Set<IndexName> indexNames = this.esService.getIndexNameByAlias(topicInfoVo.getTopicName());
            if (null != indexNames && !indexNames.isEmpty()) {
                IndexSetting indexSetting = this.esService.getIndexSetting(indexNames.iterator().next().toString());
                if (null != indexSetting) {
                    topicInfoVo.setIsOptimize(indexSetting.isOptimize());
                }
            }
        }
        return Result.ok(topicInfoVoList);
    }

    @PostMapping("listTopicDb2Es")
    @ResponseBody
    public Result<List<TopicDb2EsVo>> listTopicDb2Es() throws Exception {
        final Map<String, TopicDb2EsVo> topicDb2EsVoMap = this.topicDb2EsService.listAll();
        final List<TopicInfoVo> topicVoList = this.db2EsHttpService.getTopicVoList(null);

        for (final TopicInfoVo topicInfoVo : topicVoList) {
            final TopicDb2EsVo topicDb2EsVo = topicDb2EsVoMap.get(topicInfoVo.getTopicName());
            topicDb2EsVo.setIsActive(true);
        }

        final List<TopicDb2EsVo> result = new ArrayList<>(topicDb2EsVoMap.values());
        result.sort(Comparator.comparing(TopicDb2EsVo::getDb2esId));
        return Result.ok(result);
    }

    @PostMapping("start")
    @ResponseBody
    public synchronized Result<?> start(@RequestParam(value = "topicName") final String topicName) throws Exception {
        this.db2EsHttpService.start(topicName);
        return Result.ok();
    }

    @PostMapping("stop")
    @ResponseBody
    public synchronized Result<?> stop(@RequestParam(value = "topicName") final String topicName) throws Exception {
        this.db2EsHttpService.stop(topicName);
        return Result.ok();
    }

    @PostMapping("restart")
    @ResponseBody
    public synchronized Result<?> restart(@RequestParam(value = "topicName") final String topicName,
                                          @RequestParam(value = "offset") final String offset,
                                          @RequestParam(value = "timestamp") final String timestamp) throws Exception {
        this.db2EsHttpService.restart(topicName, offset, timestamp);
        return Result.ok();
    }

    @PostMapping("calcOffset")
    @ResponseBody
    public Result<TopicOffset> reset(@RequestParam(value = "topicName") final String topicName,
                                     @RequestParam(value = "timestamp") final Date timestamp) throws Exception {
        return Result.ok(this.db2EsHttpService.calcOffsetByTimestamp(topicName, timestamp.getTime()));
    }

    @PostMapping("listShards")
    @ResponseBody
    public Result<List<IndexVo>> listShards(@RequestParam(value = "indexName") final String indexName) {
        final List<IndexVo> indexVos = this.esService.listIndexVo(Collections.singleton(indexName));
        indexVos.sort(Comparator.comparing(o -> o.getShard().concat(o.getPrirep())));
        return Result.ok(indexVos);
    }

    @PostMapping("listSetting")
    @ResponseBody
    public Result<List<SettingVo>> listSetting(@RequestParam(value = "topicName") final String topicName) throws Exception {
        final List<SettingVo> setting = this.db2EsHttpService.getSetting(topicName);
        final NodeVo leaderNodeVo = this.db2EsHttpService.getNodeVoByTopicName(topicName);
        String slaveIp = "<span class=\"layui-badge layui-bg-orange\">无备用节点</span>";

        if (null != leaderNodeVo.getSlaveList() && !leaderNodeVo.getSlaveList().isEmpty()) {
            slaveIp = StringUtils.join(leaderNodeVo.getSlaveList().stream().map(NodeVo::getIp).collect(Collectors.toSet()), ", ");
        }

        if (!ObjectUtils.isEmpty(slaveIp)) {
            setting.add(0, new SettingVo("备用节点",
                    slaveIp,
                    String.format("id=%s的备用节点", leaderNodeVo.getId())));
        }

        return Result.ok(setting);
    }

    @PostMapping("installTopic")
    @ResponseBody
    public synchronized Result<?> installTopic(@RequestParam(value = "db2esId") final Integer db2esId,
                                               @RequestParam(value = "topicId") final Long topicId) throws Exception {
        this.topicDb2EsService.installTopic(db2esId, topicId);
        return Result.ok();
    }

    @PostMapping("uninstallTopic")
    @ResponseBody
    public synchronized Result<?> uninstallTopic(@RequestParam(value = "topicName") final String topicName) throws Exception {
        this.topicDb2EsService.uninstallTopic(topicName);
        return Result.ok();
    }

    @PostMapping("remove")
    @ResponseBody
    public Result<?> remove(@RequestParam(value = "db2esId") final Integer db2esId,
                            @RequestParam(value = "id") final Long topicId) throws Exception {
        this.topicDb2EsService.remove(db2esId, topicId);
        return Result.ok();
    }

    @PostMapping("listUnused")
    @ResponseBody
    public Result<List<Topic>> listUnused() {
        return Result.ok(this.topicDb2EsService.listUnused());
    }

    @PostMapping("diffData")
    @ResponseBody
    public Result<CompareDataResult> diffData(@RequestParam(value = "indexName") final String indexName,
                                              @RequestParam(value = "shardingValue") String shardingValue,
                                              @RequestParam(value = "pkValue") final String pkValue) throws Exception {
        if (ObjectUtils.isEmpty(pkValue.trim())) {
            throw new Db2EsException("主键值不允许为空");
        }

        final CompareDataResult compareDataResult = new CompareDataResult();
        final String shardingColumn = shardingService.listShardingColumns(indexName).iterator().next();
        Map<String, Object> esData;
        Map<String, Object> dbData = null;

        if (ObjectUtils.isEmpty(shardingValue)) {
            esData = this.esService.getElasticSearchService().getById(indexName, pkValue);
            if (null != esData) {
                shardingValue = esData.get(shardingColumn).toString();
            }
            if (!ObjectUtils.isEmpty(shardingValue)) {
                dbData = this.shardingDbService.getByIdValue(indexName, shardingValue, pkValue);
            }
        } else {
            dbData = this.shardingDbService.getByIdValue(indexName, shardingValue, pkValue);
            esData = this.esService.getElasticSearchService().getById(indexName, pkValue);
        }

        final CompareJsonVo compare = CompareUtils.compare(Collections.singletonList(dbData), Collections.singletonList(esData));
        compareDataResult.setDbData(compare.getFirst());
        compareDataResult.setEsData(compare.getSecond());
        return Result.ok(compareDataResult);
    }

    @PostMapping("sync")
    @ResponseBody
    public Result<?> sync(@RequestParam(value = "indexName") final String indexName,
                          @RequestParam(value = "shardingValue") String shardingValue,
                          @RequestParam(value = "pkValue") final String pkValue) throws Exception {
        final String shardingColumn = this.shardingService.listShardingColumns(indexName).iterator().next();

        if (ObjectUtils.isEmpty(shardingValue)) {
            final Map<String, Object> esData = this.esService.getElasticSearchService().getById(indexName, pkValue);
            if (null != esData) {
                shardingValue = esData.get(shardingColumn).toString();
            } else {
                throw new Db2EsException(String.format("在Elastic-Search中查询不到id=%s的数据", pkValue));
            }
        }

        if (ObjectUtils.isEmpty(shardingValue)) {
            throw new Db2EsException(String.format("缺少拆分键[%s]的值", shardingColumn));
        }

        final ShardingResult shardingResult = this.shardingService.doSharding(indexName, shardingValue);
        this.repairService.repair(shardingResult.getDatabaseName(),
                shardingResult.getTableName(),
                indexName,
                pkValue);
        return Result.ok();
    }

    @PostMapping("optimize")
    @ResponseBody
    public Result<?> optimize(@RequestParam(value = "topicName") final String indexAliasName) throws IOException {
        final Set<IndexName> indexNameSet = this.esService.getIndexNameByAlias(indexAliasName);
        if (null == indexNameSet || indexNameSet.isEmpty()) {
            return Result.error(String.format("不存在索引%s", indexAliasName));
        }
        final boolean result = this.esService.optimizeBulk(indexNameSet.stream().map(IndexName::toString).collect(Collectors.toSet()));
        return Result.ok(result);
    }

    @PostMapping("restore")
    @ResponseBody
    public Result<?> restore(@RequestParam(value = "topicName") final String indexAliasName) throws Exception {
        final boolean result = this.esService.unoptimizeBulk(indexAliasName);
        return Result.ok(result);
    }

    @Data
    public static class CompareDataResult {
        private String dbData;
        private String esData;
    }
}