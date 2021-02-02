package org.wyyt.kafka.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.kafka.monitor.entity.vo.KafkaConsumerVo;
import org.wyyt.kafka.monitor.entity.vo.OffsetVo;
import org.wyyt.kafka.monitor.entity.vo.RecordConsumeVo;
import org.wyyt.kafka.monitor.entity.vo.RecordVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.kafka.monitor.util.CommonUtil;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for providing the trace ability for topics' records.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Controller
@RequestMapping(RecordController.PREFIX)
public class RecordController {
    public static final String PREFIX = "record";
    private final TopicRecordService topicRecordService;
    private final KafkaService kafkaService;
    private final PropertyConfig propertyConfig;

    public RecordController(final TopicRecordService topicRecordService,
                            final KafkaService kafkaService,
                            final PropertyConfig propertyConfig) {
        this.topicRecordService = topicRecordService;
        this.kafkaService = kafkaService;
        this.propertyConfig = propertyConfig;
    }

    @GetMapping("tolist")
    public String toList(final Model model) throws Exception {
        model.addAttribute("topics", kafkaService.listTopicNames());
        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("tomsgdetail")
    public String toMsgDetail(final Model model,
                              @RequestParam(value = "topicName") final String topicName,
                              @RequestParam(value = "partitionId") final Integer partitionId,
                              @RequestParam(value = "offset") final Long offset,
                              @RequestParam(value = "key") final String key,
                              @RequestParam(value = "createTime") final Date createTime) {
        final String recordValue = this.topicRecordService.getRecordDetailValue(topicName, partitionId, offset);
        model.addAttribute("topicName", topicName);
        model.addAttribute("partitionId", partitionId);
        model.addAttribute("offset", offset);
        model.addAttribute("key", key);
        model.addAttribute("createTime", DateTool.format(createTime));
        try {
            final Object object = JSONObject.parse(recordValue);
            final String jsonValue = JSON.toJSONString(object,
                    SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat,
                    SerializerFeature.QuoteFieldNames,
                    SerializerFeature.WriteBigDecimalAsPlain);
            model.addAttribute("jsonValue", jsonValue);
        } catch (final Exception ignored) {
        }
        model.addAttribute("value", recordValue);
        return String.format("%s/msgdetail", PREFIX);
    }

    @GetMapping("toconsumerdetail")
    public String toConsumerDetail(final Model model,
                                   @RequestParam(name = "topicName", defaultValue = "") final String topicName,
                                   @RequestParam(name = "partitionId", required = false) final Integer partitionId,
                                   @RequestParam(name = "offset", required = false, defaultValue = "") final Long offset) {
        model.addAttribute("topicName", topicName.trim());
        model.addAttribute("partitionId", partitionId);
        model.addAttribute("offset", offset);
        return String.format("%s/consumerdetail", PREFIX);
    }

    @PostMapping("listTopicPartitions")
    @ResponseBody
    public Result<Set<Integer>> listTopicPartitions(@RequestParam(name = "topicName") final String topicName) throws Exception {
        return Result.ok(kafkaService.listPartitionIds(topicName));
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<RecordVo>> list(@RequestParam(name = "topicName", required = false, defaultValue = "") String topicName,
                                       @RequestParam(name = "partitionId", required = false, defaultValue = "-1") Integer partitionId,
                                       @RequestParam(name = "offset", required = false, defaultValue = "-1") Long offset,
                                       @RequestParam(name = "key", required = false, defaultValue = "") String key,
                                       @RequestParam(name = "createTimeRange", required = false, defaultValue = "") String createTimeRange,
                                       @RequestParam(value = "page") final Integer pageNum,
                                       @RequestParam(value = "limit") Integer pageSize) {
        topicName = topicName.trim();
        key = key.trim();
        createTimeRange = createTimeRange.trim();
        if (ObjectUtils.isEmpty(topicName)) {
            return Result.ok();
        }
        final IPage<RecordVo> page = new Page<>(pageNum, pageSize);
        final TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
        final Date from = timeRange.getStart(), to = timeRange.getEnd();
        try {
            this.topicRecordService.listRecords(page, topicName, partitionId, offset, key, from, to);
            return Result.ok(page.getRecords(), page.getTotal());
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }

    @PostMapping("resend")
    @ResponseBody
    public Result<?> resend(@RequestParam(name = "topicName") String topicName,
                            @RequestParam(name = "key") String key,
                            @RequestParam(name = "partitionId") Integer partitionId,
                            @RequestParam(name = "offset") Long offset) throws Exception {
        topicName = topicName.trim();
        key = key.trim();
        if (ObjectUtils.isEmpty(topicName)) {
            return Result.error("主题不能为空");
        }
        final String value = this.topicRecordService.getRecordDetailValue(topicName, partitionId, offset);
        this.kafkaService.sendMessage(topicName, key, value);
        return Result.ok();
    }

    @PostMapping("listTopicConsumers")
    @ResponseBody
    public Result<List<RecordConsumeVo>> listTopicConsumers(@RequestParam(name = "topicName", defaultValue = "") final String topicName,
                                                            @RequestParam(name = "partitionId", required = false) final Integer partitionId,
                                                            @RequestParam(name = "offset", required = false, defaultValue = "") final Long offset) throws Exception {
        final List<KafkaConsumerVo> allConsumers = this.kafkaService.listKafkaConsumers();
        final List<KafkaConsumerVo> kafkaConsumerVoList = allConsumers.stream().filter(p -> p.getTopicNames().contains(topicName)).collect(Collectors.toList());

        final List<RecordConsumeVo> result = new ArrayList<>(kafkaConsumerVoList.size());
        for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            final List<OffsetVo> offsetVoList = this.kafkaService.listOffsetVo(kafkaConsumerVoList, kafkaConsumerVo.getGroupId(), topicName);
            final Optional<OffsetVo> first = offsetVoList.stream().filter(p -> p.getPartitionId().equals(partitionId)).findFirst();
            if (first.isPresent()) {
                final OffsetVo offsetVo = first.get();
                final RecordConsumeVo recordConsumeVo = new RecordConsumeVo();
                recordConsumeVo.setGroupId(kafkaConsumerVo.getGroupId());
                recordConsumeVo.setIsConsume((offsetVo.getOffset() >= offset) ? 1 : 0);
                result.add(recordConsumeVo);
            }
        }
        return Result.ok(result);
    }
}