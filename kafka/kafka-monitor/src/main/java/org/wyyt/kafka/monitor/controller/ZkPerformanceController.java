package org.wyyt.kafka.monitor.controller;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;
import org.wyyt.kafka.monitor.entity.echarts.Series;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.kafka.monitor.entity.po.ZooKeeperPerformance;
import org.wyyt.kafka.monitor.service.dto.SysKpiService;
import org.wyyt.kafka.monitor.util.CommonUtil;
import org.wyyt.tool.cache.CacheService;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for showing the performance of zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(ZkPerformanceController.PREFIX)
public class ZkPerformanceController {
    public static final String PREFIX = "zkperformance";
    private static final List<Integer> ZK_KPI;

    static {
        ZK_KPI = new ArrayList<>(SysKpi.ZK_KPI.values().length);
        for (SysKpi.ZK_KPI value : SysKpi.ZK_KPI.values()) {
            ZK_KPI.add(value.getCode());
        }
    }

    private final PropertyConfig propertyConfig;
    private final SysKpiService sysKpiService;
    private final CacheService cacheService;

    public ZkPerformanceController(final PropertyConfig propertyConfig,
                                   final SysKpiService sysKpiService,
                                   final CacheService cacheService) {
        this.propertyConfig = propertyConfig;
        this.sysKpiService = sysKpiService;
        this.cacheService = cacheService;
    }

    @GetMapping("tolist")
    public String toList(Model model) {
        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("getChart")
    @ResponseBody
    public Result<ZooKeeperPerformance> getChart(@RequestParam(name = "createTimeRange") final String createTimeRange) {
        final String key = String.format("ZkPerformanceController::getChart::%s", createTimeRange);
        ZooKeeperPerformance result = this.cacheService.get(key);
        if (result == null) {
            result = new ZooKeeperPerformance();
            TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
            final Date from = timeRange.getStart(), to = timeRange.getEnd();
            final List<SysKpi> sysKpiList = sysKpiService.listKpi(ZK_KPI, from, to);

            result.setSend(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_PACKETS_SENT));
            result.setReceived(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_PACKETS_RECEIVED));
            result.setAlive(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_NUM_ALIVE_CONNECTIONS));
            result.setQueue(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_OUTSTANDING_REQUESTS));
            this.cacheService.put(key, result);
        }
        return Result.ok(result);
    }

    private LineInfo getInfo(final List<SysKpi> sysKpiList,
                             final SysKpi.ZK_KPI kpi) {
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