package org.wyyt.db2es.core.entity.view;

import lombok.Data;

/**
 * the view entity of Elastic-Search Information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class EsVo {
    /**
     * 目标ElasticSearch的地址, 多个用逗号隔开
     */
    private String hostnames;
    /**
     * ElasticSearch的用户名
     */
    private String username;
    /**
     * ElasticSearch的密码
     */
    private String password;
    /**
     * 创建索时,主分片的个数
     */
    private Integer numberOfShards;
    /**
     * 创建索时,每个主分片的副本分片的个数,个数越多，冗余越大,读的并发能力越强
     */
    private Integer numberOfReplicas;
}