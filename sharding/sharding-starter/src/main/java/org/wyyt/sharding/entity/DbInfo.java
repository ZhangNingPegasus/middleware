package org.wyyt.sharding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

/**
 * the entity of actual SQL statement executed
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@AllArgsConstructor
@Data
public final class DbInfo {
    private String name;
    private String databaseName;

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DbInfo dbInfo = (DbInfo) o;
        return Objects.equals(name, dbInfo.name) &&
                Objects.equals(databaseName, dbInfo.databaseName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name, databaseName);
    }
}