package org.wyyt.services;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableApolloConfig
@EnableCircuitBreaker
@EnableAsync
public class ServiceC1 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "c1");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceC1.class, args);
    }
}