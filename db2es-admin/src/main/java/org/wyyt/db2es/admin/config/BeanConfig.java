package org.wyyt.db2es.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.tool.rpc.RpcTool;

/**
 * the configuration of bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Configuration
public class BeanConfig {
    @Bean
    public RpcTool rpcTool() {
        return new RpcTool();
    }
}