package org.wyyt.sharding.auto.property;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * the entity of dimension information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class DimensionProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 维度名称
     */
    private String name;

    /**
     * 当多个拆分键在同一条SQL中出现时，维度的优先级，数值越低，优先级越高。 不允许为空。
     * 当priority="0"时，优先级最高，被视为是主维度，多个维度只能有一个主维度
     */
    private Integer priority;

    /**
     * 当前维度下的描述性信息
     */
    private String description;

    /**
     * 该维度下的所有数据源信息
     */
    private Map<String, DataSourceProperty> dataSourceProperties;
}