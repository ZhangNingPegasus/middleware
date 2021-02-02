package org.wyyt.springcloud.springbootadmin;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * the main function of SpringBootAdmin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableScheduling
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
@EnableApolloConfig
public class SpringBootAdminApplication {
    public static void main(final String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}