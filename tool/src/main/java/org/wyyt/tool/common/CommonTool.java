package org.wyyt.tool.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * the common function of resources
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class CommonTool {
    private final static long KB_IN_BYTES = 1024;
    private final static long MB_IN_BYTES = 1024 * KB_IN_BYTES;
    private final static long GB_IN_BYTES = 1024 * MB_IN_BYTES;
    private final static long TB_IN_BYTES = 1024 * GB_IN_BYTES;
    private final static long PB_IN_BYTES = 1024 * TB_IN_BYTES;
    private final static long EB_IN_BYTES = 1024 * PB_IN_BYTES;
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static void sleep(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
        }
    }

    public static double numberic(final String number) {
        return numberic(Double.valueOf(number));
    }

    public static double numberic(final Double number) {
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
        return convertSize(Math.round(numberic(number)));
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