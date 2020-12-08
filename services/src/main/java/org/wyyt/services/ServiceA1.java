package org.wyyt.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class ServiceA1 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "a1");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceA1.class, args);
    }
}