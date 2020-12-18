package org.wyyt.gateway.controller;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.gateway.anno.Auth;
import org.wyyt.gateway.entity.BaseEntity;
import org.wyyt.gateway.service.DynamicRouteService;
import org.wyyt.tool.rpc.Result;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("route")
public class RouteController {
    private final DynamicRouteService dynamicRouteService;

    public RouteController(final DynamicRouteService dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

        @Auth
    @PostMapping("refresh")
    public synchronized Mono<Result<Void>> refresh(Mono<BaseEntity> data) {
        return data.flatMap(d -> {
            this.dynamicRouteService.refresh();
            return Mono.just(Result.ok());
        });
    }

    @Auth
    @PostMapping(value = "listRoutes")
    public Mono<Result<List<String>>> listRoutes(Mono<BaseEntity> data) {
        return data.flatMap(d -> Mono.just(Result.ok(this.dynamicRouteService.listRouteDefinition().values().stream().map(RouteDefinition::toString).collect(Collectors.toList()))));
    }
}