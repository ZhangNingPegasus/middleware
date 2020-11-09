package org.wyyt.kafka.monitor.converter;

import cn.hutool.core.date.DateUtil;
import org.springframework.core.convert.converter.Converter;
import org.wyyt.tool.date.DateTool;

import java.util.Date;

/**
 * The converter of date time
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class CustomDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(final String source) {
        return DateTool.parse(source);
    }

    private Date parseDate(final String dateStr,
                           final String format) {
        return DateUtil.parse(dateStr, format);
    }
}