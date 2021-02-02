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
    public String dbHost;

    @Value("${db_port}")
    public String dbPort;

    @Value("${db_name}")
    public String dbName;

    @Value("${db_username}")
    public String dbUseName;

    @Value("${db_password}")
    public String dbPassword;

    @Value("${db_min_idle}")
    public int dbMinIdle;

    @Value("${db_maximum}")
    public int dbMaximum;

    @Value("${auth_consul_name}")
    public String authConsulName;

    @Value("${gateway_consul_name}")
    public String gatewayConsulName;

    @Value("${gateway_admin_consul_name}")
    public String gatewayAdminConsulName;
}