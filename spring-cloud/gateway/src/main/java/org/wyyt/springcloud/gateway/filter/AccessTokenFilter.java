package org.wyyt.springcloud.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.utils.Common;
import org.wyyt.springcloud.gateway.service.DataService;
import org.wyyt.springcloud.gateway.util.ResponseTool;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The filter used for checking access token which client provided.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class AccessTokenFilter implements GlobalFilter {
    private static final AntPathMatcher PATH_MATCH = new AntPathMatcher();
    private final DataService dataService;
    private final RedisService redisService;

    public AccessTokenFilter(final DataService dataService,
                             final RedisService redisService) {
        this.dataService = dataService;
        this.redisService = redisService;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange,
                             final GatewayFilterChain chain) {
        final String url = exchange.getRequest().getURI().getPath();
        if (!ObjectUtils.isEmpty(url) &&
                this.dataService.getIgnoreUrlSet().stream().anyMatch(r -> PATH_MATCH.match(r, url))) {
            return chain.filter(exchange);
        }

        final String accessToken = exchange.getRequest().getHeaders().getFirst(Names.HEADER_ACCESS_TOKEN);
        if (ObjectUtils.isEmpty(accessToken)) {
            return ResponseTool.unauthorized(exchange, String.format("%s is required", Names.HEADER_ACCESS_TOKEN));
        }

        try {
            final String clientId = Common.getClientIdFromAccessToken(accessToken);
            if (ObjectUtils.isEmpty(clientId)) {
                return ResponseTool.unauthorized(exchange, "client id is required");
            }

            final Object redisAccessToken = this.redisService.get(Constant.getAccessTokenRedisKey(clientId));
            if (ObjectUtils.isEmpty(redisAccessToken)) {
                return ResponseTool.unauthorized(exchange, String.format("%s has expired or canceled", Names.HEADER_ACCESS_TOKEN));
            } else if (!redisAccessToken.toString().equals(accessToken)) {
                return ResponseTool.unauthorized(exchange, String.format("%s has expired", Names.HEADER_ACCESS_TOKEN));
            }

            final App app = this.dataService.getApp(clientId);
            if (null == app) {
                return ResponseTool.unauthorized(exchange, String.format("client id [%s] not existed", clientId));
            }

            if (app.getIsAdmin()) {
                exchange.getRequest().mutate().header(Names.HEADER_CLIENT_ID, app.getClientId());
                return chain.filter(exchange);
            }

            Route route = null;
            String serviceName = null;
            final Object attrRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (attrRoute instanceof Route) {
                route = (Route) attrRoute;
                final Object serviceNameObj = route.getMetadata().get(Constant.SERVICE_NAME);
                if (null == serviceNameObj) {
                    return ResponseTool.unauthorized(exchange, String.format("%s is missing", Constant.SERVICE_NAME));
                }
                serviceName = serviceNameObj.toString();
            }
            List<Api> apiList;
            if (null == route) {
                apiList = this.dataService.getApiList(app.getClientId());
            } else {
                apiList = this.dataService.getApiList(app.getClientId(), serviceName);
            }
            if (apiList.stream().anyMatch(r -> PATH_MATCH.match(String.format("/**%s/**", r.getPath()), url))) {
                exchange.getRequest().mutate().header(Names.HEADER_CLIENT_ID, app.getClientId());
                return chain.filter(exchange);
            }
            return ResponseTool.unauthorized(exchange, "Access is denied");
        } catch (final SignatureException e) {
            return ResponseTool.unauthorized(exchange, String.format("%s is illegal", Names.HEADER_ACCESS_TOKEN));
        } catch (final ExpiredJwtException e) {
            return ResponseTool.unauthorized(exchange, String.format("%s is expired", Names.HEADER_ACCESS_TOKEN));
        }
    }
}