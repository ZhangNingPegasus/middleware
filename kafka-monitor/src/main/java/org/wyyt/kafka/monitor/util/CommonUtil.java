package org.wyyt.kafka.monitor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.tool.date.DateTool;

/**
 * providing the tool function.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class CommonUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static TimeRange splitTime(String timeRange) {
        if (null == timeRange) {
            return null;
        }
        timeRange = timeRange.trim();
        if (StringUtils.isEmpty(timeRange)) {
            return null;
        }
        final TimeRange result = new TimeRange();
        final String[] createTimeRanges = timeRange.split(" - ");
        result.setStart(DateTool.parse(createTimeRanges[0]));
        result.setEnd(DateTool.parse(createTimeRanges[1]));
        return result;
    }
}