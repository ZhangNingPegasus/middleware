package org.wyyt.db2es.admin.utils;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.ConvertUtils;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.db2es.admin.entity.vo.CompareJsonVo;
import org.wyyt.db2es.core.util.CommonUtils;
import org.wyyt.tool.date.DateTool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * the utils functions of compare
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class CompareUtils {
    public static CompareJsonVo compare(final List<Map<String, Object>> first,
                                        final List<Map<String, Object>> second) {
        final CompareJsonVo result = new CompareJsonVo();
        final Set<String> datetimeColumns = new HashSet<>(64);
        final Map<String, Class<?>> fieldClass = new HashMap<>(64);
        final List<LinkedHashMap<String, Object>> firstSortedMapList = new ArrayList<>(64);
        final List<LinkedHashMap<String, Object>> secondSortedMapList = new ArrayList<>(64);

        if (null != first && !first.isEmpty()) {
            for (Map<String, Object> map : first) {
                if (null == map) {
                    continue;
                }
                final LinkedHashMap<String, Object> buffer = new LinkedHashMap<>();
                for (final Map.Entry<String, Object> pair : map.entrySet()) {
                    if (null == pair.getValue()) {
                        continue;
                    }
                    if (pair.getValue().getClass().isAssignableFrom(Date.class) || pair.getValue().getClass().isAssignableFrom(DateTime.class)) {
                        datetimeColumns.add(pair.getKey());
                        pair.setValue(CommonUtils.formatMs((Date) pair.getValue()));
                    }
                }
                final List<String> keys = map.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                for (final String key : keys) {
                    final Object v = map.get(key);
                    if (null == v) {
                        continue;
                    }
                    buffer.put(key, v);
                    fieldClass.put(key, v.getClass());
                }
                firstSortedMapList.add(buffer);
            }
            if (!firstSortedMapList.isEmpty()) {
                result.setFirst(Utils.toPrettyFormatJson(JSON.toJSONString(firstSortedMapList)));
            }
        }

        if (null != second && !second.isEmpty()) {
            for (final Map<String, Object> map : second) {
                if (null == map) {
                    continue;
                }
                final LinkedHashMap<String, Object> buffer = new LinkedHashMap<>();
                for (final Map.Entry<String, Object> pair : map.entrySet()) {
                    if (datetimeColumns.contains(pair.getKey())) {
                        final Date date = DateTool.parse(pair.getValue().toString());
                        if (null == date) {
                            continue;
                        }
                        pair.setValue(CommonUtils.formatMs(date));
                    }
                }
                final List<String> keys = map.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                for (final String key : keys) {
                    Object value = map.get(key);
                    if (fieldClass.containsKey(key)) {
                        if (null == value) {
                            continue;
                        }
                        value = ConvertUtils.convert(value, fieldClass.get(key));
                    }
                    buffer.put(key, value);
                }
                secondSortedMapList.add(buffer);
            }
            if (!secondSortedMapList.isEmpty()) {
                result.setSecond(Utils.toPrettyFormatJson(JSON.toJSONString(secondSortedMapList)));
            }
        }
        return result;
    }
}