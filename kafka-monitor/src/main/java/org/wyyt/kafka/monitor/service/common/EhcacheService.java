package org.wyyt.kafka.monitor.service.common;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * the service for ehcache.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Component
public class EhcacheService implements DisposableBean {

    private final Cache<String, Object> cache;

    public EhcacheService() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public void put(final String key,
                    final Object value) {
        this.cache.put(key, value);
    }

    public <T> T get(final String key) {
        final Object value = this.cache.getIfPresent(key);
        if (null == value) {
            return null;
        }
        return (T) value;
    }

    @Override
    public void destroy() {
        if (null != this.cache) {
            this.cache.invalidateAll();
            this.cache.cleanUp();
        }
    }
}