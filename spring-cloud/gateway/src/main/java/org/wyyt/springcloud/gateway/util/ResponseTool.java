package org.wyyt.springcloud.gateway.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.ResultCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * The tool of Response
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class ResponseTool {
    public static Mono<Void> unauthorized(final ServerWebExchange exchange,
                                          final String errorMsg) {
        final ServerHttpResponse serverHttpResponse = exchange.getResponse();
        handleHeaders(serverHttpResponse.getHeaders());
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        final Result<?> result = Result.error(ResultCode.UN_AUTHORIZED.getCode(), errorMsg);
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
        Result<?> result = Result.error(ResultCode.CUSTOMIZE_ERROR.getCode(), errorMsg);
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
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        httpHeaders.set(HttpHeaders.CACHE_CONTROL, "no-cache");
    }
}
