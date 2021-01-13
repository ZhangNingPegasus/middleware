package org.wyyt.springcloud.gateway.util;


import com.alibaba.fastjson.JSON;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.wyyt.tool.rpc.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class Tool {
    public static Mono<Void> unauthorized(final ServerWebExchange exchange,
                                          final String errorMsg) {
        final ServerHttpResponse serverHttpResponse = exchange.getResponse();
        handleHeaders(serverHttpResponse.getHeaders());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        Result<?> result = Result.error(errorMsg);
        final byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        final DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }

    public static Mono<Void> unauthorized(final ServerWebExchange exchange) {
        final ServerHttpResponse serverHttpResponse = exchange.getResponse();
        handleHeaders(serverHttpResponse.getHeaders());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        return Mono.empty();
    }

    public static Mono<Void> error(final ServerWebExchange exchange,
                                   final String errorMsg) {
        final ServerHttpResponse serverHttpResponse = exchange.getResponse();
        handleHeaders(serverHttpResponse.getHeaders());
        serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        Result<?> result = Result.error(errorMsg);
        final byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        final DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }

    public static Mono<Void> error(final ServerWebExchange exchange) {
        final ServerHttpResponse serverHttpResponse = exchange.getResponse();
        handleHeaders(serverHttpResponse.getHeaders());
        serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return Mono.empty();
    }

    private static void handleHeaders(HttpHeaders httpHeaders) {
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        httpHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        httpHeaders.set(HttpHeaders.CACHE_CONTROL, "no-cache");
    }
}
