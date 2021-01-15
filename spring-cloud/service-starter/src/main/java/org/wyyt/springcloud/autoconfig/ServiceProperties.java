package org.wyyt.springcloud.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * the property of ShardingSphere configuration
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize        01/01/2021        Initialize  *
 * ******************************************************************
 */
@Data
@ConfigurationProperties("service")
public final class ServiceProperties {

}