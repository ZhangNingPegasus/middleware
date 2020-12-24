package org.wyyt.springcloud.gateway;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wyyt.admin.ui.config.EnableAdminUI;

/**
 * The main function
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableApolloConfig
@EnableAdminUI
@EnableTransactionManagement(proxyTargetClass = true)
public class GatewayAdminApplication {
    public static void main(String[] args) {
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(GatewayAdminApplication.class, args);
    }
}