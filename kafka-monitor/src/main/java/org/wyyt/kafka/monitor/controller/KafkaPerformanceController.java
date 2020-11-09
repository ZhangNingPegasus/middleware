package org.wyyt.kafka.monitor.controller;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;
import org.wyyt.kafka.monitor.entity.echarts.Series;
import org.wyyt.kafka.monitor.entity.po.KafkaPerformance;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.kafka.monitor.service.common.EhcacheService;
import org.wyyt.kafka.monitor.service.dto.SysKpiService;
import org.wyyt.kafka.monitor.util.CommonUtil;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.web.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for showing the performance of kafka's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(KafkaPerformanceController.PREFIX)
public class KafkaPerformanceController {
    public static final String PREFIX = "kafkaperformance";
    private static final List<Integer> KAFKA_KPI;

    static {
        KAFKA_KPI = new ArrayList<>(SysKpi.KAFKA_KPI.values().length);
        for (final SysKpi.KAFKA_KPI value : SysKpi.KAFKA_KPI.values()) {
            KAFKA_KPI.add(value.getCode());
        }
    }

    private final PropertyConfig propertyConfig;
    private final EhcacheService ehcacheService;
    private final SysKpiService sysKpiService;

    public KafkaPerformanceController(final PropertyConfig propertyConfig,
                                      final EhcacheService ehcacheService,
                                      final SysKpiService sysKpiService) {
        this.propertyConfig = propertyConfig;
        this.ehcacheService = ehcacheService;
        this.sysKpiService = sysKpiService;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("getChart")
    @ResponseBody
    public Result<KafkaPerformance> getZkSendChart(@RequestParam(name = "createTimeRange") final String createTimeRange) {
        final String key = String.format("KafkaPerformanceController::getZkSendChart::%s", createTimeRange);
        KafkaPerformance result = ehcacheService.get(key);
        if (null == result) {
            result = new KafkaPerformance();
            final TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
            final Date from = timeRange.getStart(), to = timeRange.getEnd();
            final List<SysKpi> sysKpiList = this.sysKpiService.listKpi(KAFKA_KPI, from, to);
            result.setMsgIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_MESSAGES_IN));
            result.setBytesIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_IN));
            result.setBytesOut(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_OUT));
            result.setBytesRejected(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_REJECTED));
            result.setFailedFetchRequest(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_FAILED_FETCH_REQUEST));
            result.setFailedProduceRequest(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_FAILED_PRODUCE_REQUEST));
            result.setProduceMessageConversions(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_PRODUCE_MESSAGE_CONVERSIONS));
            result.setTotalFetchRequests(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC));
            result.setTotalProduceRequests(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC));
            result.setReplicationBytesOut(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_REPLICATION_BYTES_OUT_PER_SEC));
            result.setReplicationBytesIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_REPLICATION_BYTES_IN_PER_SEC));
            result.setOsFreeMemory(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_OS_USED_MEMORY_PERCENTAGE));
            ehcacheService.put(key, result);
        }
        return Result.success(result);
    }

    private LineInfo getInfo(List<SysKpi> sysKpiList, SysKpi.KAFKA_KPI kpi) {
        final LineInfo result = new LineInfo();

        final List<String> times = sysKpiList.stream().map(p -> DateTool.format(p.getCollectTime())).distinct().collect(Collectors.toList());
        result.setTimes(times);
        final List<Series> seriesList = new ArrayList<>(1);
        final Series series = new Series();
        series.setName("");
        series.setType("line");
        series.setSmooth(true);
        series.setAreaStyle(new JSONObject());

        final List<Double> data = new ArrayList<>(result.getTimes().size());
        final List<Double> val = sysKpiList.stream().filter(p -> p.getKpi().equals(kpi.getCode())).map(SysKpi::getValue).collect(Collectors.toList());
        data.addAll(val);
        series.setData(data);
        seriesList.add(series);
        result.setSeries(seriesList);

        return result;
    }
}