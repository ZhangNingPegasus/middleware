package org.wyyt.springcloud.common.boot;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.wyyt.springcloud.common.config.SpringCloudConfig;
import org.wyyt.springcloud.common.service.ConsulService;
import org.wyyt.springcloud.common.service.GatewayService;
import org.wyyt.tool.rpc.RpcService;

/**
 * The configuration of services
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableAsync
@Configuration
@EnableConfigurationProperties(SpringCloudCommonProperties.class)
public class SpringCloudCommonAutoConfig {
    @Bean
    public SpringCloudConfig springCloudConfig() {
        return new SpringCloudConfig();
    }

    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }

    @Bean
    public ConsulService consulService(final SpringCloudConfig propertyConfig,
                                       final ConsulClient consulClient) {
        return new ConsulService(propertyConfig, consulClient);
    }

    @Bean
    public GatewayService gatewayService(final ConsulService consulService,
                                         final RpcService rpcService) {
        return new GatewayService(consulService, rpcService);
    }
}