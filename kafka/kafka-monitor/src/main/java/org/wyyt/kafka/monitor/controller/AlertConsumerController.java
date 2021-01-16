package org.wyyt.kafka.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.dto.SysAlertConsumer;
import org.wyyt.kafka.monitor.entity.dto.SysDingDingConfig;
import org.wyyt.kafka.monitor.entity.vo.KafkaConsumerVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysAlertConsumerService;
import org.wyyt.kafka.monitor.service.dto.SysDingDingConfigService;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for providing the ability of alert for consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(AlertConsumerController.PREFIX)
public class AlertConsumerController {
    public static final String PREFIX = "alertconsumer";
    private final SysAlertConsumerService sysAlertConsumerService;
    private final KafkaService kafkaService;
    private final SysDingDingConfigService sysDingDingConfigService;

    public AlertConsumerController(final SysAlertConsumerService sysAlertConsumerService,
                                   final KafkaService kafkaService,
                                   final SysDingDingConfigService sysDingDingConfigService) {
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.kafkaService = kafkaService;
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) throws Exception {
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers();
        final SysDingDingConfig sysDingDingConfig = this.sysDingDingConfigService.get();

        model.addAttribute("consumers", kafkaConsumerVoList);
        if (null != sysDingDingConfig) {
            model.addAttribute("accessToken", sysDingDingConfig.getAccessToken());
            model.addAttribute("secret", sysDingDingConfig.getSecret());
        }
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final String id) throws Exception {
        final SysAlertConsumer sysAlertConsumer = this.sysAlertConsumerService.getById(id);
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers();
        model.addAttribute("consumers", kafkaConsumerVoList);
        model.addAttribute("item", sysAlertConsumer);
        model.addAttribute("topics", this.listTopics(sysAlertConsumer.getGroupId(), "update").getData());
        return String.format("%s/edit", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertConsumer>> list(@RequestParam(value = "page") final Integer pageNum,
                                               @RequestParam(value = "limit") final Integer pageSize) {
        final QueryWrapper<SysAlertConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertConsumer::getRowCreateTime);
        Page<SysAlertConsumer> page = this.sysAlertConsumerService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("listTopics")
    @ResponseBody
    public Result<List<String>> listTopics(@RequestParam(value = "groupId") final String groupId,
                                           @RequestParam(value = "opt") final String opt) throws Exception {
        final List<String> result = new ArrayList<>();
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers(groupId,true);
        for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            result.addAll(kafkaConsumerVo.getTopicNames());
        }

        if ("insert".equalsIgnoreCase(opt)) {
            List<SysAlertConsumer> sysAlertConsumerList = this.sysAlertConsumerService.getByGroupId(groupId);
            result.removeAll(sysAlertConsumerList.stream().map(SysAlertConsumer::getTopicName).collect(Collectors.toList()));
        }

        result.sort(String::compareTo);
        return Result.ok(result);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "groupId") final String groupId,
                         @RequestParam(value = "topicName") final String topicName,
                         @RequestParam(value = "lagThreshold") final Long lagThreshold,
                         @RequestParam(value = "email") final String email,
                         @RequestParam(value = "accessToken") final String accessToken,
                         @RequestParam(value = "secret") final String secret
    ) {
        this.sysAlertConsumerService.save(groupId, topicName, lagThreshold, email, accessToken, secret);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "groupId") final String groupId,
                          @RequestParam(value = "topicName") final String topicName,
                          @RequestParam(value = "lagThreshold") final Long lagThreshold,
                          @RequestParam(value = "email") final String email,
                          @RequestParam(value = "accessToken") final String accessToken,
                          @RequestParam(value = "secret") final String secret
    ) {
        this.sysAlertConsumerService.update(id, groupId, topicName, lagThreshold, email, accessToken, secret);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) {
        this.sysAlertConsumerService.removeById(id);
        return Result.ok();
    }
}