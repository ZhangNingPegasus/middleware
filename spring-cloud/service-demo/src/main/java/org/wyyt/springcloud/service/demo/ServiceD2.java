package org.wyyt.springcloud.service.demo;

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
public class ServiceD2 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "d2");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceD2.class, args);
    }
}