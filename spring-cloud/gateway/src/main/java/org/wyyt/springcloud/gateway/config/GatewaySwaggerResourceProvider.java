package org.wyyt.springcloud.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * the configuration of Swagger
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Component
public class GatewaySwaggerResourceProvider implements SwaggerResourcesProvider {
    private static final String SWAGGER2_URL = "v2/api-docs";
    @Value("${spring.application.name}")
    private String applicationName;
    private final RouteLocator routeLocator;
    private final DiscoveryClient discoveryClient;

    public GatewaySwaggerResourceProvider(final RouteLocator routeLocator,
                                          final DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public List<SwaggerResource> get() {
        final List<SwaggerResource> resources = new ArrayList<>();
        final Set<String> existed = new HashSet<>();

        this.routeLocator.getRoutes().filter(
                route -> route.getUri().getHost() != null)
                .filter(route -> !this.applicationName.equals(route.getUri().getHost()))
                .subscribe(route -> {
                    final String serviceName = route.getMetadata().containsKey(Constant.SERVICE_NAME) ? route.getMetadata().get(Constant.SERVICE_NAME).toString() : route.getUri().getHost();
                    String routePath = route.getMetadata().containsKey(Constant.ROUTE_PATH) ? route.getMetadata().get(Constant.ROUTE_PATH).toString() : route.getUri().getHost();
                    routePath = StringUtils.removeStart(routePath, "/");
                    routePath = StringUtils.removeEnd(routePath, "/");
                    final String url = String.format("/%s/%s", routePath, SWAGGER2_URL);
                    if (!existed.contains(url)) {
                        final List<ServiceInstance> instances = this.discoveryClient.getInstances(serviceName);
                        final List<String> versionList = new ArrayList<>();
                        for (ServiceInstance instance : instances) {
                            versionList.add(instance.getMetadata().get(Names.VERSION));
                        }
                        final SwaggerResource swaggerResource = new SwaggerResource();
                        swaggerResource.setName(serviceName);
                        swaggerResource.setUrl(url);
                        swaggerResource.setSwaggerVersion(StringUtils.join(versionList, "; "));
                        resources.add(swaggerResource);
                        existed.add(url);
                    }
                });
        return resources;
    }
}