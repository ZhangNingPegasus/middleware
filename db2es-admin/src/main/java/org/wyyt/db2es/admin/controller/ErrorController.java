package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.vo.TimeRangeVo;
import org.wyyt.db2es.admin.entity.dto.ErrorLog;
import org.wyyt.db2es.admin.entity.vo.CompareJsonVo;
import org.wyyt.db2es.admin.service.ErrorLogService;
import org.wyyt.db2es.admin.service.common.EsService;
import org.wyyt.db2es.admin.service.common.ShardingDbService;
import org.wyyt.db2es.admin.utils.CompareUtils;
import org.wyyt.tool.rpc.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wyyt.db2es.admin.controller.ErrorController.PREFIX;

/**
 * The controller of topic page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ErrorController {
    public static final String PREFIX = "error";
    private final ShardingDbService shardingDbService;
    private final EsService esService;
    private final ErrorLogService errorLogService;

    public ErrorController(final ShardingDbService shardingDbService,
                           final EsService esService,
                           final ErrorLogService errorLogService) {
        this.shardingDbService = shardingDbService;
        this.esService = esService;
        this.errorLogService = errorLogService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("todetail")
    public String toDetail(final Model model,
                           @RequestParam(value = "id") final Long id) {
        final ErrorLog errorLog = this.errorLogService.getById(id);
        model.addAttribute("errorLog", errorLog);

        if (null != errorLog) {
            try {
                model.addAttribute("consumerRecordJson", Utils.toPrettyFormatJson(errorLog.getConsumerRecord()));
            } catch (Exception ignored) {
            }
        }

        return String.format("%s/%s", PREFIX, "detail");
    }

    @GetMapping("torepair")
    public String toRepair(final Model model,
                           @RequestParam(value = "id") final Long errorLogId) throws Exception {
        final ErrorLog errorLog = this.errorLogService.getById(errorLogId);

        if (null != errorLog) {
            final String databaseName = errorLog.getDatabaseName();
            final String tableName = errorLog.getTableName();
            final String topicName = errorLog.getTopicName();
            final String id = errorLog.getPrimaryKeyValue();

            if (!ObjectUtils.isEmpty(databaseName) &&
                    !ObjectUtils.isEmpty(tableName) &&
                    !ObjectUtils.isEmpty(id) &&
                    !ObjectUtils.isEmpty(topicName)) {
                final Map<String, Object> dbDataMap = this.shardingDbService.getById(databaseName, tableName, id);
                final Map<String, Object> esDataMap = this.esService.getElasticSearchService().getById(topicName, id);

                final CompareJsonVo compare = CompareUtils.compare(Collections.singletonList(dbDataMap), Collections.singletonList(esDataMap));
                model.addAttribute("dbJson", compare.getFirst());
                model.addAttribute("esJson", compare.getSecond());
            }
        }
        return String.format("%s/%s", PREFIX, "repair");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<ErrorLog>> list(@RequestParam(value = "primaryKeyValue", required = false) final String primaryKeyValue,
                                       @RequestParam(value = "timeRange", required = false) final String timeRange,
                                       @RequestParam(value = "isResolved", required = false) final Integer isResolved,
                                       @RequestParam(value = "topicName", required = false) final String topicName,
                                       @RequestParam(value = "partition", required = false) final Integer partition,
                                       @RequestParam(value = "offset", required = false) final Long offset,
                                       @RequestParam(value = "page") final Integer pageNum,
                                       @RequestParam(value = "limit") final Integer pageSize) {
        final TimeRangeVo timeRangeVo = Utils.splitTime(timeRange);

        final QueryWrapper<ErrorLog> queryWrapper = new QueryWrapper<>();
        final LambdaQueryWrapper<ErrorLog> lambdaQueryWrapper = queryWrapper.lambda()
                .orderByDesc(ErrorLog::getRowCreateTime);

        if (!ObjectUtils.isEmpty(primaryKeyValue)) {
            lambdaQueryWrapper.eq(ErrorLog::getPrimaryKeyValue, primaryKeyValue);
        }
        if (null != isResolved) {
            lambdaQueryWrapper.eq(ErrorLog::getIsResolved, isResolved);
        }
        if (null != timeRangeVo) {
            lambdaQueryWrapper.between(ErrorLog::getRowCreateTime, timeRangeVo.getStart(), timeRangeVo.getEnd());
        }
        if (!ObjectUtils.isEmpty(topicName)) {
            lambdaQueryWrapper.eq(ErrorLog::getTopicName, topicName);
        }
        if (null != partition) {
            lambdaQueryWrapper.eq(ErrorLog::getPartition, partition);
        }
        if (null != offset) {
            lambdaQueryWrapper.eq(ErrorLog::getOffset, offset);
        }

        final IPage<ErrorLog> page = new Page<>(pageNum, pageSize);
        this.errorLogService.page(page, queryWrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("resolve")
    @ResponseBody
    public Result<?> resolve(@RequestParam(value = "id") final Long id) {
        this.errorLogService.resolve(id);
        return Result.ok();
    }

    @PostMapping("repair")
    @ResponseBody
    public Result<?> repair(@RequestParam(value = "id") final Long errorLogId) throws Exception {
        this.errorLogService.repair(errorLogId);
        return Result.ok();
    }
}