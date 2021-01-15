package org.wyyt.springcloud.gateway.service;

import org.springframework.stereotype.Service;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.service.AppService;
import org.wyyt.springcloud.gateway.entity.service.AuthService;
import org.wyyt.springcloud.gateway.entity.service.IgnoreUrlService;
import org.wyyt.tool.cache.CacheService;

import java.util.List;
import java.util.Set;

/**
 * Providing data service
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class DataService {
    private final RedisService redisService;
    private final AuthService authService;
    private final AppService appService;
    private final IgnoreUrlService ignoreUrlService;
    private final CacheService cacheService;

    public DataService(final RedisService redisService,
                       final AuthService authService,
                       final AppService appService,
                       final IgnoreUrlService ignoreUrlService,
                       final CacheService cacheService) {
        this.redisService = redisService;
        this.authService = authService;
        this.appService = appService;
        this.ignoreUrlService = ignoreUrlService;
        this.cacheService = cacheService;
    }

    public Set<String> getIgnoreUrlSet() {
        final String key = Names.REDIS_IGNORE_URLS_KEY;
        return this.cacheService.get(key, () -> {
            final String distributedLockKey = Names.REDIS_DISTRIBUTED_LOCK_IGNORE_URLS_KEY;
            return this.redisService.getOrDefault(key, distributedLockKey, ignoreUrlService::getUrls);
        });
    }

    public void removeIngoreUrlSetLocalCache() {
        final String key = Names.REDIS_IGNORE_URLS_KEY;
        this.cacheService.delete(key);
    }

    public App getApp(final String clientId) {
        final String key = Names.getAppOfClientId(clientId);
        return this.cacheService.get(key, () -> {
            final String distributedLockKey = String.format(Names.REDIS_DISTRIBUTED_LOCK_APP_OF_CLIENT_ID_KEY, clientId);
            return this.redisService.getOrDefault(key, distributedLockKey, () -> appService.getByClientId(clientId));
        });
    }

    public void removeAppLocalCache(final String clientId) {
        final String key = Names.getAppOfClientId(clientId);
        this.cacheService.delete(key);
    }

    public List<Api> getApiList(final String clientId) {
        final String key = Names.getApiListOfClientIdKey(clientId);
        return this.cacheService.get(key, () -> {
            final String distributedLockKey = String.format(Names.REDIS_DISTRIBUTED_LOCK_API_LIST_OF_CLIENT_ID_KEY, clientId);
            return this.redisService.getOrDefault(key, distributedLockKey, () -> authService.getApiByClientId(clientId));
        });
    }

    public void removeApiListLocalCache(final String clientId) {
        final String key = Names.getApiListOfClientIdKey(clientId);
        this.cacheService.delete(key);
    }
}