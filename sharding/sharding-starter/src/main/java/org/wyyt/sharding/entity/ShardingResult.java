package org.wyyt.sharding.entity;


import lombok.Data;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.TableProperty;

/**
 * the entity of sharding result
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class ShardingResult {
    private Integer tableIndex;
    private Integer databaseIndex;
    private TableProperty.DimensionInfo tableDimensionInfo;
    private DataSourceProperty dataSourceProperty;

    public final String getDatabaseName() {
        return this.getDataSourceProperty().getDatabaseName();
    }

    public final String getTableName() {
        return String.format(this.tableDimensionInfo.getTableNameFormat(), this.tableIndex);
    }
}