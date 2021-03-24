package org.wyyt.sharding.auto.config;

import lombok.Data;

/**
 * the property of Apollo
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class ApolloConfig {

    /**
     * Apollo的Apollo_Meta
     */
    private String meta;
    /**
     * Apollo的App_Id
     */
    private String appId;

    /**
     * 数据源信息的key
     */
    private String dataSourceKey;

    /**
     * 维度信息的key
     */
    private String dimensionKey;

    /**
     * 表信息的key
     */
    private String tableKey;
}