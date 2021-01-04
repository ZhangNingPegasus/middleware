package org.wyyt.springcloud.service.demo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableApolloConfig
@EnableCircuitBreaker
@EnableAsync
public class ServiceA2 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "a2");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceA2.class, args);
    }
}