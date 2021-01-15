package org.wyyt.springcloud.gateway;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The main function
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2020        Initialize  *
 * *****************************************************************
 */
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableApolloConfig
public class GatewayApplication {
    public static void main(String[] args) {
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(GatewayApplication.class, args);
    }
}