package org.wyyt.springcloud.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.wyyt.springcloud.gateway.anno.Auth;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import reactor.core.publisher.Mono;

/**
 * The filter used for request body cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
@Order(value = -100)
public class CacheRequestBodyFilter implements WebFilter {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public CacheRequestBodyFilter(final RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange,
                             final WebFilterChain chain) {
        return requestMappingHandlerMapping
                .getHandler(exchange)
                .switchIfEmpty(chain.filter(exchange))
                .flatMap(p -> {
                    if (p instanceof HandlerMethod) {
                        final HandlerMethod handlerMethod = (HandlerMethod) p;
                        final Auth auth = handlerMethod.getMethod().getAnnotation(Auth.class);
                        if (null != auth) {
                            final Object cachedRequestBodyObject = exchange.getAttributeOrDefault(Constant.CACHED_REQUEST_BODY_OBJECT_KEY, null);
                            if (null != cachedRequestBodyObject) {
                                return chain.filter(exchange);
                            }

                            return DataBufferUtils.join(exchange.getRequest().getBody())
                                    .map(dataBuffer -> {
                                        final byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        DataBufferUtils.release(dataBuffer);
                                        return bytes;
                                    }).defaultIfEmpty(new byte[0])
                                    .doOnNext(bytes -> exchange.getAttributes().put(Constant.CACHED_REQUEST_BODY_OBJECT_KEY, bytes))
                                    .then(chain.filter(exchange));
                        }
                    }
                    return chain.filter(exchange);
                });
    }
}