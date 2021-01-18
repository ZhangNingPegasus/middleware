package org.wyyt.springcloud.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.tool.rpc.RpcService;

/**
 * the configuration of proeprty
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Getter
@Configuration
public class PropertyConfig {
    @Value("${server.port}")
    public int serverPort;

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

    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }
}
