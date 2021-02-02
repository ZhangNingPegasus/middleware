package org.wyyt.admin.ui.converter;

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
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class CustomDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(final String source) {
        return DateTool.parse(source);
    }
}