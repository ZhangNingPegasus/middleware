package org.wyyt.sql.tool.converter;

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
public final class CustomDateConverter implements Converter<String, Date> {
    @Override
    public final Date convert(final String source) {
        return DateTool.parse(source);
    }
}