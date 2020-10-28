package org.wyyt.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;
import java.util.Set;

/**
 * the domain entity of Table Name
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableNameInfo {
    private String logicTableName;
    private Set<String> factTableNameSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableNameInfo tableInfo = (TableNameInfo) o;
        return Objects.equals(logicTableName, tableInfo.logicTableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicTableName);
    }
}