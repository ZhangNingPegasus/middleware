package org.wyyt.springcloud.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * the entity of configuration information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Getter
@Configuration
public class SpringCloudConfig {
    @Value("${spring.cloud.consul.discovery.service-name}")
    private String serviceName;

    @Value("${gateway_consul_name}")
    private String gatewayConsulName;

    @Value("${gateway_url}")
    private String gatewayUrl;

    @Value("${auth_consul_name}")
    private String authConsulName;
}