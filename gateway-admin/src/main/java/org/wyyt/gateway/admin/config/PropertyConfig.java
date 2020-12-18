package org.wyyt.gateway.admin.config;

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
@Configuration
public class PropertyConfig {
    @Getter
    @Value("${db_host}")
    private String dbHost;

    @Getter
    @Value("${db_port}")
    private String dbPort;

    @Getter
    @Value("${db_name}")
    private String dbName;

    @Getter
    @Value("${db_username}")
    private String dbUid;

    @Getter
    @Value("${db_password}")
    private String dbPwd;

    @Getter
    @Value("${gateway_consul_name}")
    private String gatewayConsulName;

    @Getter
    @Value("${gateway_consul_group}")
    private String gatewayConsulGroup;

    @Getter
    @Value("${app.id}")
    private String apolloAppId;

    @Getter
    @Value("${apollo.portalUrl}")
    private String apolloPortalUrl;

    @Getter
    @Value("${apollo.token}")
    private String apolloToken;

    @Getter
    @Value("${apollo.meta}")
    private String apolloMeta;
}