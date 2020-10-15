package org.wyyt.tool.date;

import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * the common functions of Date
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * ******************************************************************
 */
public final class DateTool {
    public static Date parse(final String value) {
        return DateUtil.parse(value);
    }

    public static String format(final Date date) {
        return DateUtil.formatDateTime(date);
    }

    public static String formatMs(final Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String format(final Date date, final String format) {
        return DateUtil.format(date, format);
    }
}