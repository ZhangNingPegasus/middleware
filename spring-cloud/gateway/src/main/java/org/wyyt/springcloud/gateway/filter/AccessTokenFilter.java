package org.wyyt.springcloud.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.service.DataService;
import org.wyyt.springcloud.gateway.util.ResponseTool;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The filter used for checking access token which client provided.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
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

        final String accessToken = exchange.getRequest().getHeaders().getFirst(Names.ACCESS_TOKEN);
        if (ObjectUtils.isEmpty(accessToken)) {
            return ResponseTool.unauthorized(exchange, String.format("%s is required", Names.ACCESS_TOKEN));
        }

        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(Names.JWT_SIGNING_KEY.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody();
            final String clientId = claims.getOrDefault(Names.CLIENT_ID, "").toString();
            if (ObjectUtils.isEmpty(clientId)) {
                return ResponseTool.unauthorized(exchange, "client id is required");
            }

            final Object redisAccessToken = this.redisService.get(Names.getAccessTokenRedisKey(clientId));
            if (ObjectUtils.isEmpty(redisAccessToken)) {
                return ResponseTool.unauthorized(exchange, String.format("%s has expired or canceled", Names.ACCESS_TOKEN));
            } else if (!redisAccessToken.toString().equals(accessToken)) {
                return ResponseTool.unauthorized(exchange, String.format("%s has expired", Names.ACCESS_TOKEN));
            }

            final App app = this.dataService.getApp(clientId);
            if (null == app) {
                return ResponseTool.unauthorized(exchange, String.format("client id [%s] not existed", clientId));
            }
            if (app.getIsAdmin()) {
                return chain.filter(exchange);
            }

            Route route = null;
            String serviceId = null;
            final Object attrRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (attrRoute instanceof Route) {
                route = (Route) attrRoute;
                final Object serviceIdObj = route.getMetadata().get(Names.SERVICE_ID);
                if (null == serviceIdObj) {
                    return ResponseTool.unauthorized(exchange, String.format("%s is missing", Names.SERVICE_ID));
                }
                serviceId = serviceIdObj.toString();
            }
            List<Api> apiList;
            if (null == route) {
                apiList = this.dataService.getApiList(app.getClientId());
            } else {
                apiList = this.dataService.getApiList(app.getClientId(), serviceId);
            }
            if (apiList.stream().anyMatch(r -> PATH_MATCH.match(String.format("/**%s/**", r.getPath()), url))) {
                return chain.filter(exchange);
            }
            return ResponseTool.unauthorized(exchange, "Access is denied");
        } catch (final SignatureException e) {
            return ResponseTool.unauthorized(exchange, String.format("%s is illegal", Names.ACCESS_TOKEN));
        } catch (final ExpiredJwtException e) {
            return ResponseTool.unauthorized(exchange, String.format("%s is expired", Names.ACCESS_TOKEN));
        }
    }
}