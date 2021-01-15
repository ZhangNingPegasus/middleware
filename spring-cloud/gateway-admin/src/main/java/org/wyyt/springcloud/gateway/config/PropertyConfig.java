package org.wyyt.springcloud.gateway.config;

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
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Getter
@Configuration
public class PropertyConfig {
    @Value("${db_host}")
    private String dbHost;

    @Value("${db_port}")
    private String dbPort;

    @Value("${db_name}")
    private String dbName;

    @Value("${db_username}")
    private String dbUid;

    @Value("${db_password}")
    private String dbPwd;

    @Value("${gateway_consul_name}")
    private String gatewayConsulName;

    @Value("${gateway_consul_group}")
    private String gatewayConsulGroup;

    @Value("${auth_consul_name}")
    private String authConsulName;

    @Value("${auth_consul_group}")
    private String authConsulGroup;

    @Value("${spring.cloud.consul.discovery.service-name}")
    private String serviceName;

    @Value("${spring.cloud.consul.host}")
    private String consulHost;

    @Value("${spring.cloud.consul.port}")
    private String consulPort;

    @Value("${app.id}")
    private String apolloAppId;

    @Value("${apollo_portal_url}")
    private String apolloPortalUrl;

    @Value("${apollo_token}")
    private String apolloToken;

    @Value("${apollo_operator}")
    private String apolloOperator;

    @Value("${apollo.meta}")
    private String apolloMeta;
}