package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * the domain entity of Table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@Data
public final class TableMap {
    private final Map<TableNameInfo, TableInfo> tableMap;
    private final Map<String, TableInfo> cache;

    public TableMap() {
        this.tableMap = new HashMap<>();
        this.cache = new HashMap<>();
    }

    public final void put(final TableNameInfo key,
                          final TableInfo value) {
        this.tableMap.put(key, value);
    }

    public final TableInfo getByFactTableName(String factTableName) {
        if (!this.cache.containsKey(factTableName)) {
            for (Map.Entry<TableNameInfo, TableInfo> pair : tableMap.entrySet()) {
                if (pair.getKey().getFactTableNameSet().contains(factTableName)) {
                    cache.put(factTableName, pair.getValue());
                    break;
                }
            }
        }
        return this.cache.get(factTableName);
    }

    public final TableInfo getByLogicTableName(String logicTableName) {
        if (!this.cache.containsKey(logicTableName)) {
            for (Map.Entry<TableNameInfo, TableInfo> pair : tableMap.entrySet()) {
                if (pair.getKey().getLogicTableName().equals(logicTableName)) {
                    this.cache.put(logicTableName, pair.getValue());
                    break;
                }
            }
        }
        return this.cache.get(logicTableName);
    }
}