package org.wyyt.kafka.monitor.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;
import org.wyyt.kafka.monitor.entity.dto.SysTopicSize;
import org.wyyt.kafka.monitor.entity.echarts.LineInfo;
import org.wyyt.kafka.monitor.entity.echarts.Series;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.kafka.monitor.entity.vo.KafkaConsumerVo;
import org.wyyt.kafka.monitor.service.common.EhcacheService;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysTopicLagService;
import org.wyyt.kafka.monitor.service.dto.SysTopicSizeService;
import org.wyyt.kafka.monitor.util.CommonUtil;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.web.Result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for providing the ability of dashboard.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(DashboardController.PREFIX)
public class DashboardController {
    public static final String PREFIX = "dashboard";
    private final KafkaService kafkaService;
    private final SysTopicLagService sysTopicLagService;
    private final SysTopicSizeService sysTopicSizeService;
    private final EhcacheService ehcacheService;
    private final PropertyConfig propertyConfig;

    public DashboardController(final KafkaService kafkaService,
                               final SysTopicLagService sysTopicLagService,
                               final SysTopicSizeService sysTopicSizeService,
                               final EhcacheService ehcacheService,
                               final PropertyConfig propertyConfig) {
        this.kafkaService = kafkaService;
        this.sysTopicLagService = sysTopicLagService;
        this.sysTopicSizeService = sysTopicSizeService;
        this.ehcacheService = ehcacheService;
        this.propertyConfig = propertyConfig;
    }

    @GetMapping("index")
    public String index(Model model) throws Exception {
        model.addAttribute("savingDays", this.propertyConfig.getRetentionDays());
        model.addAttribute("topics", this.kafkaService.listTopicNames());
        return String.format("%s/index", PREFIX);
    }

    @PostMapping("getConsumersByTopicName")
    @ResponseBody
    public Result<List<KafkaConsumerVo>> getConsumersByTopicName(@RequestParam(name = "topicName", defaultValue = "") final String topicName) throws Exception {
        return Result.success(this.kafkaService.listKafkaConsumersByTopicName(topicName.trim()));
    }


    @PostMapping("getTopicSendChart")
    @ResponseBody
    public Result<LineInfo> getTopicSendChart(@RequestParam(name = "topicName") String topicName,
                                              @RequestParam(name = "createTimeRange") String createTimeRange) {
        if (ObjectUtils.isEmpty(topicName.trim()) || ObjectUtils.isEmpty(createTimeRange.trim())) {
            return Result.success(new LineInfo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        }

        final String key = String.format("DashboardController::getTopicSendChart:%s:%s", topicName, createTimeRange);
        final LineInfo cache = this.ehcacheService.get(key);
        if (cache != null) {
            return Result.success(cache);
        }

        topicName = topicName.trim();
        createTimeRange = createTimeRange.trim();
        if (ObjectUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        final LineInfo result = new LineInfo();
        final TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();
        from = DateUtils.addMinutes(from, -1);
        final List<SysTopicSize> sysTopicSizeList = this.sysTopicSizeService.listByTopicName(topicName, from, to);
        final List<String> topicNames = sysTopicSizeList.stream().map(SysTopicSize::getTopicName).distinct().collect(Collectors.toList());
        final List<String> times = sysTopicSizeList.stream().map(p -> DateTool.format(p.getRowCreateTime())).distinct().collect(Collectors.toList());
        if (times.size() > 0) {
            times.remove(0);
        }
        result.setTopicNames(topicNames);
        result.setTimes(times);

        final List<Series> seriesList = new ArrayList<>(result.getTopicNames().size());
        for (final String name : result.getTopicNames()) {
            final Series series = new Series();
            series.setName(name);
            series.setType("line");
            series.setSmooth(true);
            seriesList.add(series);
        }
        result.setSeries(seriesList);

        for (final Series series : result.getSeries()) {
            final List<SysTopicSize> topicSysLogSize = sysTopicSizeList.stream().filter(p -> p.getTopicName().equals(series.getName())).collect(Collectors.toList());
            final List<Double> data = new ArrayList<>(result.getTimes().size());
            for (final String time : result.getTimes()) {
                Long preLogSize = null;
                Long curLogSize = null;
                Date preDate = null;
                Date curDate = null;
                for (int i = 0; i < topicSysLogSize.size(); i++) {
                    final SysTopicSize sysLogSize = topicSysLogSize.get(i);
                    if (sysLogSize.getTopicName().equals(series.getName()) && DateTool.format(sysLogSize.getRowCreateTime()).equals(time)) {
                        curLogSize = sysLogSize.getLogSize();
                        curDate = sysLogSize.getRowCreateTime();
                        int preIndex = i - 1;
                        if (preIndex >= 0 && preIndex < topicSysLogSize.size()) {
                            preLogSize = topicSysLogSize.get(preIndex).getLogSize();
                            preDate = topicSysLogSize.get(preIndex).getRowCreateTime();
                        }
                        break;
                    }
                }
                Long logSize = null;
                if (curLogSize != null) {
                    logSize = curLogSize - (preLogSize == null ? 0 : preLogSize);
                    if (logSize < 0) {
                        logSize = 0L;
                    }
                }
                double seconds = 60.0D;
                if (curDate != null && preDate != null) {
                    seconds = (curDate.getTime() - preDate.getTime()) / 1000.0D;
                }

                if (null == logSize) {
                    data.add(null);
                } else {
                    data.add(CommonTool.numberic(logSize / seconds));
                }
            }
            series.setData(data);
        }
        this.ehcacheService.put(key, result);
        return Result.success(result);
    }

    @PostMapping("getLagChart")
    @ResponseBody
    public Result<LineInfo> getLagChart(@RequestParam(name = "topicName") String topicName,
                                        @RequestParam(name = "groupId") String groupId,
                                        @RequestParam(name = "createTimeRange") String createTimeRange) {
        final String key = String.format("DashboardController::getLagChart:%s:%s", groupId, createTimeRange);
        final LineInfo cache = this.ehcacheService.get(key);
        if (null != cache) {
            return Result.success(cache);
        }
        groupId = groupId.trim();
        createTimeRange = createTimeRange.trim();
        if (ObjectUtils.isEmpty(groupId) || ObjectUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        LineInfo result = new LineInfo();
        final TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();

        final List<SysTopicLag> sysTopicLagList = this.sysTopicLagService.listByGroupId(topicName, groupId, from, to);
        final List<String> topicNames = sysTopicLagList.stream().map(SysTopicLag::getTopicName).distinct().collect(Collectors.toList());
        final List<String> times = sysTopicLagList.stream().map(p -> DateTool.format(p.getRowCreateTime())).distinct().collect(Collectors.toList());

        result.setTopicNames(topicNames);
        result.setTimes(times);

        final List<Series> seriesList = new ArrayList<>(result.getTopicNames().size());
        for (final String name : result.getTopicNames()) {
            final Series series = new Series();
            series.setName(name);
            series.setType("line");
            series.setSmooth(true);
            seriesList.add(series);
        }
        result.setSeries(seriesList);

        for (final Series series : result.getSeries()) {
            final List<Double> data = new ArrayList<>(result.getTimes().size());
            for (final String time : result.getTimes()) {
                final List<Double> lag = sysTopicLagList.stream().filter(p -> p.getTopicName().equals(series.getName()) && DateTool.format(p.getRowCreateTime()).equals(time)).map(p -> Double.parseDouble(p.getLag().toString())).collect(Collectors.toList());
                if (lag.size() < 1) {
                    data.add(null);
                } else {
                    data.addAll(lag);
                }
            }
            series.setData(data);
        }
        this.ehcacheService.put(key, result);
        return Result.success(result);
    }

    @PostMapping("getConsumeTpsChart")
    @ResponseBody
    public Result<LineInfo> getConsumeTpsChart(@RequestParam(name = "topicName") String topicName,
                                               @RequestParam(name = "groupId") String groupId,
                                               @RequestParam(name = "createTimeRange") String createTimeRange) {
        String key = String.format("DashboardController::getConsumeTpsChart:%s:%s", groupId, createTimeRange);
        LineInfo cache = this.ehcacheService.get(key);
        if (cache != null) {
            return Result.success(cache);
        }
        groupId = groupId.trim();
        createTimeRange = createTimeRange.trim();
        if (ObjectUtils.isEmpty(groupId) || ObjectUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        LineInfo result = new LineInfo();
        TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();

        final List<SysTopicLag> sysTopicLagList = this.sysTopicLagService.listByGroupId(topicName, groupId, from, to);
        final List<String> topicNames = sysTopicLagList.stream().map(SysTopicLag::getTopicName).distinct().collect(Collectors.toList());
        final List<String> times = sysTopicLagList.stream().map(p -> DateTool.format(p.getRowCreateTime())).distinct().collect(Collectors.toList());
        if (times.size() > 0) {
            times.remove(0);
        }
        result.setTopicNames(topicNames);
        result.setTimes(times);

        List<Series> seriesList = new ArrayList<>(result.getTopicNames().size());
        for (final String name : result.getTopicNames()) {
            final Series series = new Series();
            series.setName(name);
            series.setType("line");
            series.setSmooth(true);
            seriesList.add(series);
        }
        result.setSeries(seriesList);

        for (final Series series : result.getSeries()) {
            final List<SysTopicLag> sysLags = sysTopicLagList.stream().filter(p -> p.getTopicName().equals(series.getName())).collect(Collectors.toList());
            final List<Double> data = new ArrayList<>(result.getTimes().size());
            for (final String time : result.getTimes()) {
                Long preLogSize = null;
                Long curLogSize = null;
                Date preDate = null;
                Date curDate = null;
                for (int i = 0; i < sysLags.size(); i++) {
                    final SysTopicLag sysLag = sysLags.get(i);
                    if (sysLag.getTopicName().equals(series.getName()) && DateTool.format(sysLag.getRowCreateTime()).equals(time)) {
                        curLogSize = sysLag.getOffset();
                        curDate = sysLag.getRowCreateTime();
                        int preIndex = i - 1;
                        if (preIndex >= 0 && preIndex < sysLags.size()) {
                            preLogSize = sysLags.get(preIndex).getOffset();
                            preDate = sysLags.get(preIndex).getRowCreateTime();
                        }
                        break;
                    }
                }
                Long offset = null;
                if (curLogSize != null) {
                    offset = curLogSize - (preLogSize == null ? 0 : preLogSize);
                    if (offset < 0) {
                        offset = 0L;
                    }
                }
                double seconds = 60.0D;
                if (curDate != null && preDate != null) {
                    seconds = (curDate.getTime() - preDate.getTime()) / 1000.0D;
                }

                if (offset == null) {
                    data.add(null);
                } else {
                    data.add(CommonTool.numberic(offset / seconds));
                }
            }
            series.setData(data);
        }
        this.ehcacheService.put(key, result);
        return Result.success(result);
    }


    @PostMapping("getTopicRankChart")
    @ResponseBody
    public Result<LineInfo> getTopicRankChart(@RequestParam(name = "createTimeRange") String createTimeRange) {
        final String key = String.format("DashboardController::getTopicRankChart:%s", createTimeRange);
        final LineInfo cache = this.ehcacheService.get(key);
        if (null != cache) {
            return Result.success(cache);
        }
        createTimeRange = createTimeRange.trim();
        if (ObjectUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        final TimeRange timeRange = CommonUtil.splitTime(createTimeRange);
        final Date from = timeRange.getStart(), to = timeRange.getEnd();
        final LineInfo result = new LineInfo();
        final List<SysTopicSize> sysLogSizeList = this.sysTopicSizeService.getTopicRank(5, from, to);
        final List<String> topicNames = sysLogSizeList.stream().map(SysTopicSize::getTopicName).collect(Collectors.toList());
        final List<Double> logSizeList = sysLogSizeList.stream().map(p -> Double.parseDouble(p.getLogSize().toString())).collect(Collectors.toList());
        final List<Series> seriesList = new ArrayList<>();
        final Series series = new Series();
        series.setType("bar");
        series.setData(logSizeList);
        seriesList.add(series);

        result.setTopicNames(topicNames);
        result.setSeries(seriesList);
        this.ehcacheService.put(key, result);
        return Result.success(result);
    }

    @PostMapping("getTopicHistoryChart")
    @ResponseBody
    @TranRead
    public Result<LineInfo> getTopicHistoryChart(@RequestParam(name = "topicName") String topicName) {
        topicName = topicName.trim();
        if (ObjectUtils.isEmpty(topicName)) {
            return Result.success();
        }

        final LineInfo result = new LineInfo();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        final Long[] daysValue = new Long[8];
        daysValue[0] = this.sysTopicSizeService.getHistoryLogSize(topicName, 0);
        daysValue[1] = this.sysTopicSizeService.getHistoryLogSize(topicName, 1);
        daysValue[2] = this.sysTopicSizeService.getHistoryLogSize(topicName, 2);
        daysValue[3] = this.sysTopicSizeService.getHistoryLogSize(topicName, 3);
        daysValue[4] = this.sysTopicSizeService.getHistoryLogSize(topicName, 4);
        daysValue[5] = this.sysTopicSizeService.getHistoryLogSize(topicName, 5);
        daysValue[6] = this.sysTopicSizeService.getHistoryLogSize(topicName, 6);
        daysValue[7] = this.sysTopicSizeService.getHistoryLogSize(topicName, 7);

        final List<String> timesList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            final Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE), 0, 0, 0);
            final Date date = DateUtils.addDays(calendar.getTime(), -i);
            timesList.add(sdf.format(date));
        }
        final List<Double> data = new ArrayList<>();
        for (final Long aLong : daysValue) {
            data.add((double) aLong);
        }

        final List<Series> seriesList = new ArrayList<>();
        final Series series = new Series();
        series.setType("bar");
        series.setData(data);
        seriesList.add(series);

        result.setTimes(timesList);
        result.setSeries(seriesList);
        return Result.success(result);
    }
}