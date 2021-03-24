package org.wyyt.tool.common;

import com.alibaba.fastjson.JSONValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.util.ObjectUtils;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

/**
 * the common function of resources
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class CommonTool {
    private final static long KB_IN_BYTES = 1024;
    private final static long MB_IN_BYTES = 1024 * KB_IN_BYTES;
    private final static long GB_IN_BYTES = 1024 * MB_IN_BYTES;
    private final static long TB_IN_BYTES = 1024 * GB_IN_BYTES;
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static boolean isJson(final String value) {
        if (ObjectUtils.isEmpty(value)) {
            return false;
        }

        try (final JSONValidator from = JSONValidator.from(value)) {
            return from.validate();
        } catch (IOException e) {
            return false;
        }
    }

    public static Date getMinDate() {
        return new Date(0);
    }

    public static <T> List<T> parseList(final String value,
                                        final String separate,
                                        final Class<T> tClass) {
        final String[] array = value.split(separate);
        List<T> result = new ArrayList<>(array.length);
        for (final String item : array) {
            if (ObjectUtils.isEmpty(item)) {
                continue;
            }
            result.add((T) ConvertUtils.convert(item, tClass));
        }
        return result;
    }

    public static void sleep(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
        }
    }

    public static double toDouble(final String number) {
        return toDouble(Double.valueOf(number));
    }

    public static double toDouble(final Double number) {
        return Double.parseDouble(DECIMAL_FORMAT.format(number));
    }

    public static String convertSize(long byteNumber) {
        if (byteNumber / TB_IN_BYTES > 0) {
            return String.format("%sTB", DECIMAL_FORMAT.format((double) byteNumber / (double) TB_IN_BYTES));
        } else if (byteNumber / GB_IN_BYTES > 0) {
            return String.format("%sGB", DECIMAL_FORMAT.format((double) byteNumber / (double) GB_IN_BYTES));
        } else if (byteNumber / MB_IN_BYTES > 0) {
            return String.format("%sMB", DECIMAL_FORMAT.format((double) byteNumber / (double) MB_IN_BYTES));
        } else if (byteNumber / KB_IN_BYTES > 0) {
            return String.format("%sKB", DECIMAL_FORMAT.format((double) byteNumber / (double) KB_IN_BYTES));
        } else {
            return String.format("%sB", byteNumber);
        }
    }

    public static String convertSize(String number) {
        return convertSize(Math.round(toDouble(number)));
    }

    public static Map<String, Object> queryParamstoMap(final String queryParams) {
        final Map<String, Object> result = new HashMap<>();
        if (ObjectUtils.isEmpty(queryParams)) {
            return result;
        }
        final String query;
        try {
            query = URLDecoder.decode(queryParams.trim(), StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final String[] pairs = query.split("&");
        for (final String pair : pairs) {
            final String[] kv = pair.split("=");
            if (2 != kv.length) {
                continue;
            }
            result.put(kv[0], kv[1]);
        }
        return result;
    }

    public static Map<String, Object> queryParamstoMap(final byte[] queryParamsByte) {
        String queryParams = "";
        if (null != queryParamsByte) {
            queryParams = new String(queryParamsByte);
        }
        return queryParamstoMap(queryParams);
    }

    public static Map<String, Object> queryParamstoMap(final Object queryParamsObject) {
        if (null == queryParamsObject) {
            return new HashMap<>();
        } else if (queryParamsObject instanceof byte[]) {
            return queryParamstoMap((byte[]) queryParamsObject);
        } else if (queryParamsObject instanceof String) {
            return queryParamstoMap((String) queryParamsObject);
        }
        throw new RuntimeException(String.format("%s type not supported", queryParamsObject.getClass()));
    }
}