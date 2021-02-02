package org.wyyt.sharding.db2es.admin.rebuild;

import org.wyyt.sharding.db2es.core.entity.domain.IndexName;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * The tool for rebuild
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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