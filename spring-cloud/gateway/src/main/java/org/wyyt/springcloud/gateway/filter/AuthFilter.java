package org.wyyt.springcloud.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.wyyt.springcloud.gateway.anno.Auth;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.util.ResponseTool;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.SignTool;
import reactor.core.publisher.Mono;

/**
 * The filter used for permission checking
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Order(-1)
@Component
public class AuthFilter implements WebFilter {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public AuthFilter(final RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return requestMappingHandlerMapping
                .getHandler(exchange)
                .switchIfEmpty(chain.filter(exchange))
                .flatMap(p -> {
                    if (p instanceof HandlerMethod) {
                        final HandlerMethod handlerMethod = (HandlerMethod) p;
                        final Auth auth = handlerMethod.getMethod().getAnnotation(Auth.class);
                        if (null != auth) {
                            final String sign = exchange.getRequest().getHeaders().getFirst("sign");
                            if (ObjectUtils.isEmpty(sign)) {
                                return ResponseTool.unauthorized(exchange);
                            }

                            try {
                                final Object queryParamsObject = exchange.getAttributeOrDefault(Names.CACHED_REQUEST_BODY_OBJECT_KEY, null);
                                if (SignTool.checkSign(sign, CommonTool.queryParamstoMap(queryParamsObject), Names.API_KEY, Names.API_IV)) {
                                    return chain.filter(exchange);
                                }
                                return ResponseTool.unauthorized(exchange);
                            } catch (final Exception exception) {
                                return Mono.error(exception);
                            }
                        }
                    }
                    return chain.filter(exchange);
                });
    }
}