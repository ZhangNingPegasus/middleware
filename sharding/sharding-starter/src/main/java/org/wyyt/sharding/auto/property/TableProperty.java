package org.wyyt.sharding.auto.property;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * the entity of sharding table inforamtion
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class TableProperty {
    /**
     * 数据表的逻辑名称
     */
    private String name;

    /**
     * 主键字段名
     */
    private String pkName;

    /**
     * 记录创建时间字段(时间精确到毫秒)
     */
    private String rowCreateTime;

    /**
     * 记录最后一次修改时间字段(时间精确到毫秒)
     */
    private String rowUpdateTime;

    /**
     * 具有相同绑定名称的表为一组绑定表。可以为空, 为空表示不和任何表组成绑定表
     */
    private String bindingGroupName;

    /**
     * 是否是广播表。
     */
    private Boolean broadcast;

    /**
     * 该数据表在不同维度的信息. key: 维度名称; value: 维度信息
     */
    private Map<String, DimensionInfo> dimensionInfos;

    @Data
    public static class DimensionInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private DimensionProperty dimensionProperty;
        private Integer tableCountNum;
        private String tableNameFormat;
        private String shardingColumn;
    }
}