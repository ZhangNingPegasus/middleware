package org.wyyt.springcloud.boot;

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
@ConfigurationProperties("spring.cloud")
public final class SpringCloudApplicationProperties {
    public static final String DEFAULT_THREAD_NAME_PREFIX = "Pool-Executor-";

    /**
     * Executor的线程核心个数, 默认: 当前机器的可用核心数
     */
    private Integer corePoolSize;

    /**
     * Executor的最大线程个数, 默认: 当前机器的可用核心数 * 5
     */
    private Integer maxPoolSize;

    /**
     * Executor的最大等待线程个数, 默认: 当前机器的可用核心数 * 2
     */
    private Integer queueCapacity;

    /**
     * Executor的线程前缀, 默认: Pool-Executor-
     */
    private String threadNamePrefix;
}