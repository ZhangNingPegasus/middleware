package org.wyyt.sharding.db2es.admin.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * the view object of rebuild index
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class RebuildVo {
    /**
     * 数据表的逻辑名称
     */
    private String name;

    /**
     * 是否正在重建索引中
     */
    private boolean inRebuild;

    /**
     * 是否应该禁用重建
     */
    private boolean disableRebuild;

    /**
     * 主分片的个数
     */
    private Integer numberOfShards;

    /**
     * 每个主分片的副本分片的个数
     */
    private Integer numberOfReplicas;

    /**
     * 数据刷盘的间隔时间
     */
    private String refreshInterval;

    /**
     * 将多少年的索引归为同一个索引别名
     */
    private Integer aliasOfYears;

    /**
     * 主题描述信息
     */
    private String description;

    /**
     * 最后一次创建时间
     */
    public Date rowUpdateTime;
}