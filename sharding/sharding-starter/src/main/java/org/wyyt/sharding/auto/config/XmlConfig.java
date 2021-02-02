package org.wyyt.sharding.auto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * the property of ShardingSphere configuration
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ConfigurationProperties("sharding")
public final class XmlConfig {
    /**
     * 是否开启ShardingSphere数据源
     */
    private Boolean enabled = false;

    /**
     * 分布式集群编号id, 不能重复(取值范围0~1023)
     */
    private Long workId = 0L;

    /**
     * 是否输出执行的sql(true:打印; false:不打印)
     */
    private Boolean showSql = false;

    /**
     * ACM配置信息
     */
    private AcmConfig acm;
}