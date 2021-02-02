package org.wyyt.kafka.monitor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.entity.po.TimeRange;
import org.wyyt.tool.date.DateTool;

/**
 * providing the tool function.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class CommonUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static TimeRange splitTime(String timeRange) {
        if (null == timeRange) {
            return null;
        }
        timeRange = timeRange.trim();
        if (ObjectUtils.isEmpty(timeRange)) {
            return null;
        }
        final TimeRange result = new TimeRange();
        final String[] createTimeRanges = timeRange.split(" - ");
        result.setStart(DateTool.parse(createTimeRanges[0]));
        result.setEnd(DateTool.parse(createTimeRanges[1]));
        return result;
    }
}