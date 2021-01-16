package org.wyyt.sharding.auto.property;

import lombok.Data;

/**
 * the entity of ShardingSphere data source
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class DataSourceProperty {
    /**
     * 数据库的逻辑名称
     */
    private String name;

    /**
     * 数据库的索引(分库时使用), 从0开始, 默认为0
     */
    private Integer index;

    /**
     * 数据库IP地址
     */
    private String host;

    /**
     * 数据库端口
     */
    private Integer port;

    /**
     * 数据库的真实物理名称
     */
    private String databaseName;

    /**
     * 数据库的账号
     */
    private String username;

    /**
     * 数据库的密码
     */
    private String password;

    /**
     * 是否是默认数据源。true:是；false:不是。非必填，默认为false
     */
    private Boolean isDefault;

    /**
     * 配置连接池中最小可用连接的个数
     */
    private Integer minIdle;

    /**
     * 配置连接池中最大可用连接的个数
     */
    private Integer maxActive;
}