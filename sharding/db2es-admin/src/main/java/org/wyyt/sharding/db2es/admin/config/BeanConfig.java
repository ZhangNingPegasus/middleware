package org.wyyt.sharding.db2es.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.tool.rpc.RpcService;

/**
 * the configuration of bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
public class BeanConfig {
    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }
}