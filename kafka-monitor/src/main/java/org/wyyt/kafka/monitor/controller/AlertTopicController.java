package org.wyyt.kafka.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.dto.SysAlertTopic;
import org.wyyt.kafka.monitor.entity.dto.SysDingDingConfig;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysAlertTopicService;
import org.wyyt.kafka.monitor.service.dto.SysDingDingConfigService;
import org.wyyt.tool.web.Result;

import java.util.Comparator;
import java.util.List;


/**
 * The controller for providing the ability of alert for topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(AlertTopicController.PREFIX)
public class AlertTopicController {
    public static final String PREFIX = "alerttopic";
    private final SysAlertTopicService sysAlertTopicService;
    private final KafkaService kafkaService;
    private final SysDingDingConfigService sysDingDingConfigService;

    public AlertTopicController(final SysAlertTopicService sysAlertTopicService,
                                final KafkaService kafkaService,
                                final SysDingDingConfigService sysDingDingConfigService) {
        this.sysAlertTopicService = sysAlertTopicService;
        this.kafkaService = kafkaService;
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) throws Exception {
        final List<String> topicNameList = this.sysAlertTopicService.listTopicNames();
        final List<String> allTopicNameList = this.kafkaService.listTopicNames();
        final SysDingDingConfig sysDingDingConfig = this.sysDingDingConfigService.get();

        allTopicNameList.removeAll(this.sysAlertTopicService.listTopicNames());
        allTopicNameList.sort(String::compareTo);

        model.addAttribute("topics", allTopicNameList);
        if (null != sysDingDingConfig) {
            model.addAttribute("accessToken", sysDingDingConfig.getAccessToken());
            model.addAttribute("secret", sysDingDingConfig.getSecret());
        }
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final String id) throws Exception {
        final SysAlertTopic sysAlertTopic = this.sysAlertTopicService.getById(id);
        final List<String> topicNameList = this.sysAlertTopicService.listTopicNames();
        final List<String> allTopicNameList = this.kafkaService.listTopicNames();
        allTopicNameList.removeAll(topicNameList);
        if (null != sysAlertTopic) {
            allTopicNameList.add(sysAlertTopic.getTopicName());
            allTopicNameList.sort(Comparator.naturalOrder());
        }
        model.addAttribute("topics", allTopicNameList);
        model.addAttribute("item", sysAlertTopic);
        return String.format("%s/edit", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertTopic>> list(@RequestParam(value = "page") final Integer pageNum,
                                            @RequestParam(value = "limit") final Integer pageSize) {
        final QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertTopic::getRowCreateTime);
        Page<SysAlertTopic> page = this.sysAlertTopicService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page.getRecords(), page.getTotal());
    }

    @PostMapping("save")
    @ResponseBody
    public Result<?> save(@RequestParam(value = "id", required = false) final Long id,
                          @RequestParam(value = "topicName", required = false) final String topicName,
                          @RequestParam(value = "fromTime", required = false) final String fromTime,
                          @RequestParam(value = "toTime", required = false) final String toTime,
                          @RequestParam(value = "fromTps", required = false) final Integer fromTps,
                          @RequestParam(value = "toTps", required = false) final Integer toTps,
                          @RequestParam(value = "fromMomTps", required = false) final Integer fromMomTps,
                          @RequestParam(value = "toMomTps", required = false) final Integer toMomTps,
                          @RequestParam(value = "email", required = false) final String email,
                          @RequestParam(value = "accessToken", required = false) final String accessToken,
                          @RequestParam(value = "secret", required = false) final String secret
    ) {
        if (fromTps == null &&
                toTps == null &&
                fromMomTps == null &&
                toMomTps == null) {
            return Result.error("TPS设置至少需要填写一个");
        }

        if (id == null) {
            this.sysAlertTopicService.save(topicName, fromTime, toTime, fromTps, toTps, fromMomTps, toMomTps, email, accessToken, secret);
        } else {
            this.sysAlertTopicService.update(id, topicName, fromTime, toTime, fromTps, toTps, fromMomTps, toMomTps, email, accessToken, secret);
        }
        return Result.success();
    }


    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) {
        this.sysAlertTopicService.removeById(id);
        return Result.success();
    }
}