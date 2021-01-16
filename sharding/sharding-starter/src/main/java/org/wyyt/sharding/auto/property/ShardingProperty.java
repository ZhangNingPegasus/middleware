package org.wyyt.sharding.auto.property;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * the entity of ShardingSphere configuration information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class ShardingProperty {
    /**
     * 是否打印SQL
     */
    private boolean showSql;

    /**
     * ShardingSphere的数据源信息. key: 数据源逻辑名称; value: 数据源属性信息
     */
    private Map<String, DataSourceProperty> dataSourceProperties;

    /**
     * 维度信息. key: 维度名称; value: 维度信息
     */
    private Map<String, DimensionProperty> dimensionProperties;

    /**
     * 数据表信息
     */
    private List<TableProperty> tableProperties;
}