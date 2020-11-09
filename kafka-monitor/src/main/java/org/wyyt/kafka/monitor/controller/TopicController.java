package org.wyyt.kafka.monitor.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.vo.MBeanVo;
import org.wyyt.kafka.monitor.entity.vo.PartitionVo;
import org.wyyt.kafka.monitor.entity.vo.TopicVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for providing the ability of creating, modifying and deleting the topics.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Controller
@RequestMapping(TopicController.PREFIX)
public class TopicController {
    public static final String PREFIX = "topic";
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final PropertyConfig propertyConfig;

    public TopicController(final KafkaService kafkaService,
                           final TopicRecordService topicRecordService,
                           final PropertyConfig propertyConfig) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.propertyConfig = propertyConfig;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        int brokerSize = 1;
        try {
            brokerSize = this.kafkaService.listBrokerInfos().size();
        } catch (final Exception exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
        }
        model.addAttribute("brokerSize", brokerSize);
        model.addAttribute("replicasNum", brokerSize);
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "topicName") String topicName) throws Exception {
        topicName = topicName.trim();
        final List<PartitionVo> partitionVoList = this.kafkaService.listTopicDetails(topicName, false);
        if (null != partitionVoList) {
            for (PartitionVo partitionVo : partitionVoList) {
                model.addAttribute("replicasNum", partitionVo.getReplicas().size());
                break;
            }
        }
        model.addAttribute("topicName", topicName);
        model.addAttribute("partitionNum", this.kafkaService.listPartitionIds(topicName).size());
        return String.format("%s/edit", PREFIX);
    }

    @GetMapping("todetail")
    public String toDetail(final Model model,
                           @RequestParam(name = "topicName") final String topicName) {
        model.addAttribute("topicName", topicName.trim());
        return String.format("%s/detail", PREFIX);
    }

    @GetMapping("tosendmsg")
    public String toSendMsg(Model model,
                            @RequestParam(name = "topicName") final String topicName) {
        model.addAttribute("topicName", topicName.trim());
        return String.format("%s/sendmsg", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<TopicVo>> list(@RequestParam(value = "topicName", required = false) String searchTopicName,
                                      @RequestParam(value = "page") Integer pageNum,
                                      @RequestParam(value = "limit") Integer pageSize) throws Exception {
        pageNum = Math.min(pageNum, Constants.MAX_PAGE_NUM);

        List<String> topicNames = this.kafkaService.listTopicNames();
        if (!StringUtils.isEmpty(searchTopicName)) {
            topicNames = topicNames.stream().filter(p -> p.toLowerCase().contains(searchTopicName.toLowerCase())).collect(Collectors.toList());
        }

        final List<String> currentPageTopicNames = topicNames.stream()
                .skip(pageSize * (pageNum - 1))
                .limit(pageSize)
                .sorted(String::compareTo)
                .collect(Collectors.toList());

        final List<TopicVo> topicInfoList = this.kafkaService.listTopicVos(currentPageTopicNames);
        return Result.success(topicInfoList, topicNames.size());
    }

    @PostMapping("sendmsg")
    @ResponseBody
    public Result<?> sendMsg(@RequestParam(value = "topicName") String topicName,
                             @RequestParam(value = "content") String content) throws Exception {
        this.kafkaService.sendMessage(topicName.trim(), content.trim());
        return Result.success();
    }

    @PostMapping("listTopicDetails")
    @ResponseBody
    public Result<List<PartitionVo>> listTopicDetails(@RequestParam(name = "topicName") String topicName) throws Exception {
        return Result.success(this.kafkaService.listTopicDetails(topicName.trim(), true));
    }

    @PostMapping("listTopicSize")
    @ResponseBody
    public Result<String> listTopicSize(@RequestParam(name = "topicName") String topicName) throws Exception {
        return Result.success(this.kafkaService.getTopicDiskSpace(topicName.trim()));
    }

    @PostMapping("listTopicDetailLogSize")
    @ResponseBody
    public Result<List<TopicVo>> listTopicDetailLogSize(@RequestParam(name = "topicName") String topicName) {
        final TopicVo topicInfoList = this.topicRecordService.listTopicDetailLogSize(topicName);
        return Result.success(new ArrayList<>(Collections.singleton(topicInfoList)));
    }

    @PostMapping("listTopicMBean")
    @ResponseBody
    public Result<List<MBeanVo>> listTopicMBean(@RequestParam(name = "topicName") String topicName) throws Exception {
        return Result.success(this.kafkaService.listTopicMBean(topicName.trim()));
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(name = "topicName") final String topicName,
                         @RequestParam(name = "partitionNumber") final Integer partitionNumber,
                         @RequestParam(name = "replicationNumber") final Integer replicationNumber) throws Exception {
        this.kafkaService.createTopic(topicName, partitionNumber, replicationNumber);
        return Result.success();
    }


    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(name = "topicName") String topicName,
                          @RequestParam(name = "partitionCount") Integer partitionCount,
                          @RequestParam(name = "replicationCount", required = false) Integer replicationCount) throws Exception {
        this.kafkaService.editTopic(topicName.trim(), partitionCount, replicationCount);
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "topicNames") final String topicNames) throws Exception {
        this.topicRecordService.deleteTopic(Arrays.asList(topicNames.split(",")));
        return Result.success();
    }
}