package org.wyyt.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * the main function of SpringBootAdmin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
//@EnableApolloConfig
public class SpringBootAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}