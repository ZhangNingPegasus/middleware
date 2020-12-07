package org.wyyt.gateway;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableApolloConfig
public class GatewayApplication {
    public static void main(String[] args) {
        System.setProperty("nepxion.banner.shown", "false");
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("discovery-guide-service-a", r -> r
                        .path("/discovery-guide-service-a/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://discovery-guide-service-a"))
                .build();
    }
}