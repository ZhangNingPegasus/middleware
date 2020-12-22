package org.wyyt.gateway.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.apollo.tool.ApolloTool;
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
    private final PropertyConfig propertyConfig;

    public BeanConfig(PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    public RpcTool rpcTool() {
        return new RpcTool();
    }

    @Bean
    public ApolloTool apolloTool() {
        return new ApolloTool(
                this.propertyConfig.getApolloPortalUrl(),
                this.propertyConfig.getApolloMeta(),
                this.propertyConfig.getApolloToken(),
                this.propertyConfig.getApolloAppId(),
                this.propertyConfig.getApolloOperator());
    }
}
