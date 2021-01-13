package org.wyyt.springcloud.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
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
import org.wyyt.springcloud.gateway.entity.service.AppService;
import org.wyyt.springcloud.gateway.entity.service.AuthService;
import org.wyyt.springcloud.gateway.entity.service.IgnoreUrlService;
import org.wyyt.springcloud.gateway.util.Tool;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class AccessTokenFilter implements GlobalFilter {
    private static final AntPathMatcher PATH_MATCH = new AntPathMatcher();
    private final AuthService authService;
    private final AppService appService;
    private final IgnoreUrlService ignoreUrlService;
    private final RedisService redisService;

    public AccessTokenFilter(final AuthService authService,
                             final AppService appService,
                             final IgnoreUrlService ignoreUrlService,
                             final RedisService redisService) {
        this.authService = authService;
        this.appService = appService;
        this.ignoreUrlService = ignoreUrlService;
        this.redisService = redisService;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange,
                             final GatewayFilterChain chain) {
        final String url = exchange.getRequest().getURI().getPath();
        if (!ObjectUtils.isEmpty(url) &&
                this.getIgnoreUrlSet().stream().anyMatch(r -> PATH_MATCH.match(r, url))) {
            return chain.filter(exchange);
        }

        final String accessToken = exchange.getRequest().getHeaders().getFirst(Names.ACCESS_TOKEN);
        if (ObjectUtils.isEmpty(accessToken)) {
            return Tool.unauthorized(exchange, String.format("%s is required", Names.ACCESS_TOKEN));
        }

        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(Names.JWT_SIGNING_KEY.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody();
            final String clientId = claims.getOrDefault("client_id", "").toString();
            if (ObjectUtils.isEmpty(clientId)) {
                return Tool.unauthorized(exchange, "client id is required");
            }

            final Object redisAccessToken = this.redisService.get(String.format(Names.REDIS_ACCESS_TOKEN_KEY, clientId));
            if (ObjectUtils.isEmpty(redisAccessToken)) {
                return Tool.unauthorized(exchange, String.format("%s has expired", Names.ACCESS_TOKEN));
            } else if (!redisAccessToken.toString().equals(accessToken)) {
                return Tool.unauthorized(exchange, String.format("%s has expired", Names.ACCESS_TOKEN));
            }

            final App app = this.getApp(clientId);
            if (null == app) {
                return Tool.unauthorized(exchange, String.format("client id [%s] not existed", clientId));
            }
            if (app.getIsAdmin()) {
                return chain.filter(exchange);
            }

            final List<Api> apiList = getApiList(app.getClientId());
            if (apiList.stream().anyMatch(r -> PATH_MATCH.match(String.format("/**%s/**", r.getPath()), url))) {
                return chain.filter(exchange);
            }
            return Tool.unauthorized(exchange, "Access is denied");
        } catch (final SignatureException e) {
            return Tool.unauthorized(exchange, String.format("%s is illegal", Names.ACCESS_TOKEN));
        }
    }

    private Set<String> getIgnoreUrlSet() {
        final String key = Names.REDIS_IGNORE_URLS_KEY;
        final String distributedLockKey = Names.REDIS_DISTRIBUTED_LOCK_IGNORE_URLS_KEY;
        return this.redisService.getOrDefault(key, distributedLockKey, ignoreUrlService::getUrls);
    }

    private App getApp(final String clientId) {
        final String key = String.format(Names.REDIS_APP_OF_CLIENT_ID_KEY, clientId);
        final String distributedLockKey = String.format(Names.REDIS_DISTRIBUTED_LOCK_APP_OF_CLIENT_ID_KEY, clientId);
        return this.redisService.getOrDefault(key, distributedLockKey, () -> appService.getByClientId(clientId));
    }

    private List<Api> getApiList(final String clientId) {
        final String key = String.format(Names.REDIS_API_LIST_OF_CLIENT_ID_KEY, clientId);
        final String distributedLockKey = String.format(Names.REDIS_DISTRIBUTED_LOCK_API_LIST_OF_CLIENT_ID_KEY, clientId);
        return this.redisService.getOrDefault(key, distributedLockKey, () -> authService.getApiByClientId(clientId));
    }
}