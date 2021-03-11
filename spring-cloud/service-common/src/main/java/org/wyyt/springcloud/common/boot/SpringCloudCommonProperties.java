package org.wyyt.springcloud.common.boot;

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
@ConfigurationProperties(prefix = "spring.cloud.config")
public final class SpringCloudCommonProperties {
}