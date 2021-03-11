package org.wyyt.springcloud.gateway.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.anno.Auth;
import org.wyyt.springcloud.gateway.entity.BaseEntity;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.service.DataService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * the controller of Dynamic Routing
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@RestController
@RequestMapping("cache")
public class LocalCacheController {
    private final DataService dataService;

    public LocalCacheController(final DataService dataService) {
        this.dataService = dataService;
    }

    @Auth
    @PostMapping("removeIgnoreUrlSetLocalCache")
    public Mono<Result<Void>> removeIgnoreUrlSetLocalCache(final Mono<BaseEntity> data) {
        return data.flatMap(d -> {
            this.dataService.removeIgnoreUrlSetLocalCache();
            return Mono.just(Result.ok());
        });
    }

    @Auth
    @PostMapping("removeClientIdLocalCache")
    public Mono<Result<Void>> removeClientIdLocalCache(final Mono<BaseEntity> data,
                                                       final ServerWebExchange exchange) {
        return data.flatMap(d -> {
            final Object queryParamsObject = exchange.getAttributeOrDefault(Constant.CACHED_REQUEST_BODY_OBJECT_KEY, null);
            final Map<String, Object> params = CommonTool.queryParamstoMap(queryParamsObject);
            this.dataService.removeApiListLocalCache(params.get(Names.CLIENT_ID).toString());
            this.dataService.removeAppLocalCache(params.get(Names.CLIENT_ID).toString());
            return Mono.just(Result.ok());
        });
    }
}