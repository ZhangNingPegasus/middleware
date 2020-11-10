package org.wyyt.kafka.monitor.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.echarts.Style;
import org.wyyt.kafka.monitor.entity.echarts.TreeInfo;
import org.wyyt.kafka.monitor.entity.po.Offset;
import org.wyyt.kafka.monitor.entity.vo.KafkaConsumerVo;
import org.wyyt.kafka.monitor.entity.vo.OffsetVo;
import org.wyyt.kafka.monitor.entity.vo.TopicVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.kafka.monitor.util.CommonUtil;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for providing the ability of consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Controller
@RequestMapping(ConsumerController.PREFIX)
public class ConsumerController {
    public static final String PREFIX = "consumer";
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;

    public ConsumerController(final KafkaService kafkaService,
                              final TopicRecordService topicRecordService) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("todetail")
    public String toDetail(final Model model,
                           @RequestParam(name = "groupId") final String groupId) {
        model.addAttribute("groupId", groupId.trim());
        return String.format("%s/detail", PREFIX);
    }

    @GetMapping("toeditoffset")
    public String toEditOffset(final Model model,
                               @RequestParam(name = "groupId") final String groupId,
                               @RequestParam(name = "topicName") final String topicName) {
        model.addAttribute("groupId", groupId.trim());
        model.addAttribute("topicName", topicName.trim());
        return String.format("%s/editoffset", PREFIX);
    }


    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaConsumerVo>> list(final HttpSession httpSession,
                                              @RequestParam(value = "groupId", required = false, defaultValue = "") final String searchGroupId,
                                              @RequestParam(value = "page") final Integer pageNum,
                                              @RequestParam(value = "limit") final Integer pageSize) {
        try {
            final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers(searchGroupId.trim());

            final List<KafkaConsumerVo> currentPage = kafkaConsumerVoList.stream()
                    .skip(pageSize * (pageNum - 1))
                    .limit(pageSize)
                    .sorted(Comparator.comparing(KafkaConsumerVo::getGroupId))
                    .collect(Collectors.toList());
            httpSession.setAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO, currentPage);
            return Result.success(currentPage, kafkaConsumerVoList.size());
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return Result.success();
        }
    }

    @PostMapping("getChartData")
    @ResponseBody
    public Result<TreeInfo> getChartData(final HttpSession httpSession) {
        final TreeInfo root = new TreeInfo("消费者 - 主题");
        root.setStyle(Style.info());
        final List<KafkaConsumerVo> kafkaConsumerVoList = (List<KafkaConsumerVo>) httpSession.getAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        httpSession.removeAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        if (null != kafkaConsumerVoList) {
            final List<TreeInfo> consuerGroupTreeInfoList = new ArrayList<>(kafkaConsumerVoList.size());

            for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
                final TreeInfo consumerGroup = new TreeInfo(kafkaConsumerVo.getGroupId());
                if (kafkaConsumerVo.getNotActiveTopicNames().size() == kafkaConsumerVo.getTopicNames().size()) {
                    consumerGroup.setStyle(Style.warn());
                } else if (kafkaConsumerVo.getActiveTopicNames().size() == kafkaConsumerVo.getTopicNames().size()) {
                    consumerGroup.setStyle(Style.success());
                } else {
                    consumerGroup.setStyle(Style.info());
                }

                final List<TreeInfo> topicTreeInfoList = new ArrayList<>(kafkaConsumerVo.getTopicCount());
                for (final String activeTopicName : kafkaConsumerVo.getActiveTopicNames()) {
                    final TreeInfo topicInfo = new TreeInfo(activeTopicName);
                    topicInfo.setStyle(Style.success());
                    topicTreeInfoList.add(topicInfo);
                }

                for (final String notActiveTopicName : kafkaConsumerVo.getNotActiveTopicNames()) {
                    final TreeInfo topicInfo = new TreeInfo(notActiveTopicName);
                    topicInfo.setItemStyle(Style.warn());
                    topicInfo.setLineStyle(Style.warn());
                    topicTreeInfoList.add(topicInfo);
                }
                consumerGroup.setChildren(topicTreeInfoList);
                consuerGroupTreeInfoList.add(consumerGroup);
            }
            root.setChildren(consuerGroupTreeInfoList);
        }
        if (null != root.getChildren() && root.getChildren().size() > 0) {
            return Result.success(root);
        } else {
            return Result.success();
        }
    }

    @PostMapping("listConsumerDetails")
    @ResponseBody
    public Result<List<TopicVo>> listConsumerDetails(@RequestParam(name = "groupId") String groupId) throws Exception {
        groupId = groupId.trim();
        final List<TopicVo> result = new ArrayList<>();

        final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers(groupId);

        if (null != kafkaConsumerVoList && !kafkaConsumerVoList.isEmpty()) {
            final KafkaConsumerVo kafkaConsumerVo = kafkaConsumerVoList.get(0);
            for (final String topicName : kafkaConsumerVo.getActiveTopicNames()) {
                final TopicVo topicVo = new TopicVo();
                topicVo.setTopicName(topicName);
                topicVo.setConsumerStatus(1);
                result.add(topicVo);
            }
            for (final String notActiveTopicName : kafkaConsumerVo.getNotActiveTopicNames()) {
                final TopicVo topicVo = new TopicVo();
                topicVo.setTopicName(notActiveTopicName);
                topicVo.setConsumerStatus(0);
                result.add(topicVo);
            }
        }

        for (final TopicVo topicVo : result) {
            long lag = 0L;
            try {
                final List<OffsetVo> offsetVoList = this.kafkaService.listOffsetVo(kafkaConsumerVoList, groupId, topicVo.getTopicName());
                for (final OffsetVo offsetVo : offsetVoList) {
                    if (null != offsetVo.getLag() && offsetVo.getLag() > 0) {
                        lag += offsetVo.getLag();
                    }
                    if (offsetVo.getLogSize() < 0L) {
                        topicVo.setError(offsetVo.getConsumerId());
                    }
                }
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                lag = -1L;
            }
            topicVo.setLag(lag);
        }
        return Result.success(result);
    }

    @PostMapping("listOffsetVo")
    @ResponseBody
    public Result<List<OffsetVo>> listOffsetVo(@RequestParam(name = "groupId") final String groupId,
                                               @RequestParam(name = "topicName") final String topicName) {
        try {
            return Result.success(this.kafkaService.listOffsetVo(null, groupId.trim(), topicName.trim()));
        } catch (final Exception exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            return Result.success();
        }
    }


    @PostMapping("editOffset")
    @ResponseBody
    public Result<?> editOffset(@RequestParam(name = "groupId") final String groupId,
                                @RequestParam(name = "offsets") final String offsets) throws Exception {
        final List<Offset> offsetList = CommonUtil.OBJECT_MAPPER.readValue(offsets, new TypeReference<List<Offset>>() {
        });
        try {
            this.kafkaService.alterOffset(groupId, offsetList);
        } catch (final Exception exception) {
            return Result.error(String.format("偏移量修改失败, 原因:%s", ExceptionTool.getRootCauseMessage(exception)));
        }
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(name = "consumerGroupIds") final String consumerGroupIds) {
        String errMsg = this.topicRecordService.deleteConsumer(new HashSet<>(Arrays.asList(consumerGroupIds.split(","))));
        if (StringUtils.isEmpty(errMsg)) {
            return Result.success();
        } else {
            return Result.error(String.format("部分消费者删除失败, 原因: %s", errMsg));
        }
    }
}