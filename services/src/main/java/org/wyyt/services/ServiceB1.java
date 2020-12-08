package org.wyyt.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceB1 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "b1");
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(ServiceB1.class, args);
    }
}