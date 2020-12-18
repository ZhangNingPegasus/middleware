package org.wyyt.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.wyyt.gateway.admin.business.contants.Names;
import org.wyyt.gateway.anno.Auth;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.SignTool;
import reactor.core.publisher.Mono;

/**
 * The filter used for permission checking
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Order(-1)
@Component
public class PermissionFilter implements WebFilter {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public PermissionFilter(final RequestMappingHandlerMapping requestMappingHandlerMapping) {
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
                                return unauthorized(exchange);
                            }

                            try {
                                final Object queryParamsObject = exchange.getAttributeOrDefault(FilterConstant.CACHED_REQUEST_BODY_OBJECT_KEY, null);
                                if (SignTool.checkSign(sign, CommonTool.queryParamstoMap(queryParamsObject), Names.API_KEY, Names.API_IV)) {
                                    return chain.filter(exchange);
                                }
                                return unauthorized(exchange);
                            } catch (final Exception exception) {
                                return Mono.error(exception);
                            }
                        }
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> unauthorized(final ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return Mono.empty();
    }
}