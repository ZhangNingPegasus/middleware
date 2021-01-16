package org.wyyt.tool.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * the wapper class of cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class CacheService implements DisposableBean {
    private final Cache<String, Object> cache;

    public CacheService() {
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
                .initialCapacity(64)   // 设置缓存容器的初始容量
                .maximumSize(1024)    // 设置缓存最大容量，超过之后就会按照LRU最近虽少使用算法来移除缓存项
                .removalListener(notification -> log.info(String.format("%s was removed, cause is %s", notification.getKey(), notification.getCause()))) //设置缓存的移除通知
                .build();
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
                           final Callable<? extends T> callable) {
        Object value;
        try {
            value = this.cache.get(key, callable);
        } catch (ExecutionException e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            throw new RuntimeException(e);
        }
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