package org.wyyt.springcloud.springbootadmin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * the data source configuration
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
public class PropertyConfig {
    @Value("${db_host}")
    private String dbHost;

    @Value("${db_port}")
    private String dbPort;

    @Value("${db_name}")
    private String dbName;

    @Value("${db_username}")
    private String dbUseName;

    @Value("${db_password}")
    private String dbPassword;

    @Value("${db_min_idle}")
    private int dbMinIdle;

    @Value("${db_maximum}")
    private int dbMaximum;

    @Value("${auth_consul_name}")
    private String authConsulName;

    @Value("${gateway_consul_name}")
    private String gatewayConsulName;

    @Value("${gateway_admin_consul_name}")
    private String gatewayAdminConsulName;
}