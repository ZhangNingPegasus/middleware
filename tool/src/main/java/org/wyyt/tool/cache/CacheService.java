package org.wyyt.tool.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * the wapper class of cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class CacheService implements DisposableBean {
    private final Cache<String, Object> cache;

    public CacheService(final Long expireAfterAccessInMinutes,
                        final Integer initialCapacity,
                        final Long maximumSize) {
        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (null != expireAfterAccessInMinutes) {
            caffeine.expireAfterAccess(expireAfterAccessInMinutes, TimeUnit.MINUTES);  //多少分钟不访问就过期
        }
        if (null != initialCapacity) {
            caffeine.initialCapacity(initialCapacity);   // 设置缓存容器的初始容
        }
        if (null != maximumSize) {
            caffeine.maximumSize(maximumSize);    // 设置缓存最大容量，超过之后就会按照LRU最近虽少使用算法来移除缓存项
        }
        this.cache = caffeine.build();
    }

    public CacheService() {
        this(30L, 64, 1024L);
    }

    public final void put(final String key,
                          final Object value) {
        this.cache.put(key, value);
    }

    public final <T> T get(final String key) {
        final Object value = this.cache.getIfPresent(key);
        if (null == value) {
            return null;
        }
        return (T) value;
    }

    public final <T> T get(final String key,
                           final Function<String, Object> function) {
        final Object value = this.cache.get(key, function);
        if (null == value) {
            return null;
        }
        return (T) value;
    }

    public final void delete(final String key) {
        this.cache.invalidate(key);
    }

    @Override
    public void destroy() {
        if (null != this.cache) {
            this.cache.invalidateAll();
            this.cache.cleanUp();
        }
    }
}