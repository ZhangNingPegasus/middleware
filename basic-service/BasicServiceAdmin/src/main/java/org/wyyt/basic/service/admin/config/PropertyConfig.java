package org.wyyt.basic.service.admin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * the entity of configuration information in application.yml
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
    private String dbUid;

    @Value("${db_password}")
    private String dbPwd;

    @Value("${db_min_idle}")
    private int dbMinIdle;

    @Value("${db_maximum}")
    private int dbMaximum;

    @Value("${sms_consul_name}")
    private String smsConsulName;
}