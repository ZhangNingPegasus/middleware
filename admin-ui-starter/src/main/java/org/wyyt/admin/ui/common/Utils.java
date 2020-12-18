package org.wyyt.admin.ui.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.entity.vo.TimeRangeVo;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * collection tools
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class Utils {
    private final static String SALT = "PEgASuS";

    public static String getVersion() {
        return System.getProperty("version", "BETA");
    }

    public static String getCliectIp(final HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || "".equals(ip.trim()) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || "".equals(ip.trim()) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || "".equals(ip.trim()) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        final String[] arr = ip.split(",");
        for (final String str : arr) {
            if (!"unknown".equalsIgnoreCase(str)) {
                ip = str;
                break;
            }
        }
        return ip;
    }

    public static <T> T toVo(final Object source,
                             final Class<T> target) {
        if (null == source) {
            return null;
        }
        try {
            final T result = target.newInstance();
            BeanUtils.copyProperties(source, result);
            return result;
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return null;
        }
    }

    public static <T> List<T> toVoList(final List<?> source,
                                       final Class<T> target) {
        if (null == source) {
            return null;
        }
        try {
            final List<T> result = new ArrayList<>(source.size());
            for (final Object o : source) {
                result.add(toVo(o, target));
            }
            return result;
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return null;
        }
    }

    public static String hash(final String value) {
        return new Md5Hash(value, SALT).toString();
    }

    public static String toPrettyFormatJson(final String json) {
        final JSONValidator.Type type = JSONValidator.from(json).getType();
        if (JSONValidator.Type.Array == type) {
            return JSON.toJSONString(JSONArray.parse(json),
                    SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat,
                    SerializerFeature.QuoteFieldNames,
                    SerializerFeature.WriteBigDecimalAsPlain);
        } else if (null == type || JSONValidator.Type.Object == type) {
            return JSON.toJSONString(JSONObject.parseObject(json),
                    SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat,
                    SerializerFeature.QuoteFieldNames,
                    SerializerFeature.WriteBigDecimalAsPlain);
        } else {
            return json;
        }
    }

    public static TimeRangeVo splitTime(String timeRange) {
        if (null == timeRange) {
            return null;
        }
        timeRange = timeRange.trim();
        if (ObjectUtils.isEmpty(timeRange)) {
            return null;
        }
        final TimeRangeVo result = new TimeRangeVo();
        final String[] createTimeRanges = timeRange.split(" - ");
        result.setStart(DateTool.parse(createTimeRanges[0]));
        result.setEnd(DateTool.parse(createTimeRanges[1]));
        return result;
    }
}