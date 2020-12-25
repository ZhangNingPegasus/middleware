package org.wyyt.sharding.db2es.core.entity.persistent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Map;
import java.util.Objects;

/**
 * The entity for table t_topic
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
@TableName(value = "`t_topic`")
public final class Topic extends BaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 主题名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 主分片的个数
     */
    @TableField(value = "`number_of_shards`")
    private Integer numberOfShards;

    /**
     * 每个主分片的副本分片的个数
     */
    @TableField(value = "`number_of_replicas`")
    private Integer numberOfReplicas;

    /**
     * 数据刷盘的间隔时间
     */
    @TableField(value = "`refresh_interval`")
    private String refreshInterval;

    /**
     * 将多少年的索引归为同一个索引别名
     */
    @TableField(value = "`alias_of_years`")
    private Integer aliasOfYears;

    /**
     * 主题对应的ES索引的MAPPING
     */
    @TableField(value = "`mapping`")
    private String mapping;

    /**
     * 主题描述信息
     */
    @TableField(value = "`description`")
    private String description;

    /**
     * 正在使用的索引后缀. KEY: 年份; VALUE: 后缀
     */
    @TableField(exist = false)
    private Map<Integer, Integer> inUseSuffixMap;

    /**
     * 用于索引重建的索引后缀. KEY: 年份; VALUE: 后缀
     */
    @TableField(exist = false)
    private Map<Integer, Integer> rebuildSuffixMap;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Topic topic = (Topic) o;
        return name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}