package org.wyyt.sharding.db2es.admin.rebuild;

import org.wyyt.sharding.db2es.core.entity.domain.IndexName;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * The tool for rebuild
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class Tool {
    public static String getIndexName(final Map<Integer, IndexName> rebuildIndexMap,
                                      final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int year = calendar.get(Calendar.YEAR);
        IndexName indexName = rebuildIndexMap.get(year);
        if (null == indexName) {
            return null;
        }
        return indexName.toString();
    }
}