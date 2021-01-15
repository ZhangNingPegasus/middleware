package org.wyyt.springcloud.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GatewaySwaggerResourceProvider implements SwaggerResourcesProvider {
    private static final String SWAGGER2_URL = "/v2/api-docs";
    private final RouteLocator routeLocator;
    @Value("${spring.application.name}")
    private String applicationName;

    public GatewaySwaggerResourceProvider(final RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        final List<SwaggerResource> resources = new ArrayList<>();
        final List<String> routeHosts = new ArrayList<>();
        this.routeLocator.getRoutes().filter(
                route -> route.getUri().getHost() != null)
                .filter(route -> !applicationName.equals(route.getUri().getHost()))
                .subscribe(route -> routeHosts.add(route.getUri().getHost()));

        final Set<String> existed = new HashSet<>();
        routeHosts.forEach(instance -> {
            final String url = String.format("/%s%s", instance, SWAGGER2_URL);
            if (!existed.contains(url)) {
                existed.add(url);
                final SwaggerResource swaggerResource = new SwaggerResource();
                swaggerResource.setUrl(url);
                swaggerResource.setName(instance);
                resources.add(swaggerResource);
            }
        });
        return resources;
    }
}