package org.wyyt.kafka.monitor.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.entity.echarts.CpuInfo;
import org.wyyt.kafka.monitor.entity.echarts.ThreadInfo;
import org.wyyt.kafka.monitor.entity.vo.TopicRecordCountVo;
import org.wyyt.kafka.monitor.service.common.EhcacheService;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysKpiService;
import org.wyyt.kafka.monitor.service.dto.SysTopicSizeService;
import org.wyyt.tool.common.CommonTool;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller for big screen.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(BigScreenController.PREFIX)
public class BigScreenController {
    public static final String PREFIX = "bigscreen";

    private final PropertyConfig propertyConfig;
    private final SysKpiService sysKpiService;
    private final KafkaService kafkaService;
    private final SysTopicSizeService sysTopicSizeService;
    private final EhcacheService ehcacheService;

    public BigScreenController(final PropertyConfig propertyConfig,
                               final SysKpiService sysKpiService,
                               final KafkaService kafkaService,
                               final SysTopicSizeService sysTopicSizeService,
                               final EhcacheService ehcacheService) {
        this.propertyConfig = propertyConfig;
        this.sysKpiService = sysKpiService;
        this.kafkaService = kafkaService;
        this.sysTopicSizeService = sysTopicSizeService;
        this.ehcacheService = ehcacheService;
    }

    @GetMapping("tolist")
    public String toList(Model model) throws Exception {
        Long day0 = this.ehcacheService.get("BigScreenController::day0");
        Integer zkCount = this.ehcacheService.get("BigScreenController::zkCount");
        Integer kafkaCount = this.ehcacheService.get("BigScreenController::kafkaCount");
        Integer topicCount = this.ehcacheService.get("BigScreenController::topicCount");
        List<TopicRecordCountVo> topicRecordCountVoList = this.ehcacheService.get("BigScreenController::topicRecordCountVoList");
        CpuInfo cpuInfo = this.ehcacheService.get("BigScreenController::cpuInfo");
        ThreadInfo threadInfo = this.ehcacheService.get("BigScreenController::threadInfo");

        if (null == day0 || null == zkCount || null == kafkaCount || null == topicCount || null == topicRecordCountVoList || null == cpuInfo || null == threadInfo) {
            day0 = this.sysTopicSizeService.getTotalRecordCount(3);
            zkCount = this.propertyConfig.getZkServers().split(",").length;
            kafkaCount = this.kafkaService.listBrokerNames().size();
            topicCount = this.kafkaService.listTopicNames().size();
            topicRecordCountVoList = this.sysTopicSizeService.listTotalRecordCount(18);
            for (final TopicRecordCountVo topicRecordCountVo : topicRecordCountVoList) {
                final String topicName = topicRecordCountVo.getTopicName();
                int i = topicName.lastIndexOf(".");
                if (i >= 0) {
                    if (i + 1 <= topicName.length() - 1) {
                        topicRecordCountVo.setTopicName(topicName.substring(i + 1));
                    }
                }
            }

            final Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    0,
                    0,
                    0);
            final Date from = calendar.getTime();
            final Date to = DateUtils.addDays(from, 1);

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            final List<SysKpi> sysKpiList = this.sysKpiService.listKpi(Arrays.asList(SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode(), SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode()),
                    DateUtils.addMinutes(new Date(), -60),
                    to
            );
            cpuInfo = new CpuInfo();
            cpuInfo.setXAxis(sysKpiList.stream().map(p -> sdf.format(p.getCollectTime())).distinct().collect(Collectors.toList()));
            cpuInfo.setSystemCpu(sysKpiList.stream().filter(p -> p.getKpi() == SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode()).map(p -> CommonTool.numberic(p.getValue())).collect(Collectors.toList()));
            cpuInfo.setProcessCpu(sysKpiList.stream().filter(p -> p.getKpi() == SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode()).map(p -> CommonTool.numberic(p.getValue())).collect(Collectors.toList()));

            cpuInfo.setStrXAxis(JSON.toJSONString(cpuInfo.getXAxis()));
            cpuInfo.setStrSystemCpu(JSON.toJSONString(cpuInfo.getSystemCpu()));
            cpuInfo.setStrProcessCpu(JSON.toJSONString(cpuInfo.getProcessCpu()));

            final List<SysKpi> threadInfoList = sysKpiService.listKpi(Collections.singletonList(SysKpi.KAFKA_KPI.KAFKA_THREAD_COUNT.getCode()), DateUtils.addMinutes(new Date(), -60), to);
            threadInfo = new ThreadInfo();
            threadInfo.setXAxis(threadInfoList.stream().map(p -> sdf.format(p.getCollectTime())).distinct().collect(Collectors.toList()));
            threadInfo.setThreadCount(threadInfoList.stream().map(p -> p.getValue().intValue()).collect(Collectors.toList()));

            threadInfo.setStrXAxis(JSON.toJSONString(threadInfo.getXAxis()));
            threadInfo.setStrThreadCount(JSON.toJSONString(threadInfo.getThreadCount()));

            this.ehcacheService.put("BigScreenController::day0", day0);
            this.ehcacheService.put("BigScreenController::zkCount", zkCount);
            this.ehcacheService.put("BigScreenController::kafkaCount", kafkaCount);
            this.ehcacheService.put("BigScreenController::topicCount", topicCount);
            this.ehcacheService.put("BigScreenController::topicRecordCountVoList", topicRecordCountVoList);
            this.ehcacheService.put("BigScreenController::cpuInfo", cpuInfo);
            this.ehcacheService.put("BigScreenController::threadInfo", threadInfo);
        }

        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        model.addAttribute("totalRecordCount", day0);
        model.addAttribute("zkCount", zkCount);
        model.addAttribute("kafkaCount", kafkaCount);
        model.addAttribute("topicCount", topicCount);
        model.addAttribute("topicRecordCountVoList", topicRecordCountVoList);
        model.addAttribute("cpuInfo", cpuInfo);
        model.addAttribute("threadInfo", threadInfo);
        return String.format("%s/%s", PREFIX, "list");
    }

}
