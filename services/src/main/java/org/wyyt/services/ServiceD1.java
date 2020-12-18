package org.wyyt.services;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableApolloConfig
@EnableCircuitBreaker
public class ServiceD1 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "d1");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceD1.class, args);
    }
}