package org.wyyt.sharding.db2es.client.cache;

import org.wyyt.tool.cache.CacheService;

import java.io.Closeable;

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
public class CacheWrapper implements Closeable {
    private final CacheService cacheService;

    public CacheWrapper() {
        this.cacheService = new CacheService();
    }

    public final void put(final String key,
                          final Object value) {
        this.cacheService.put(key, value);
    }

    public final <T> T get(final String key) {
        final Object value = this.cacheService.get(key);
        if (null == value) {
            return null;
        }
        return (T) value;
    }

    @Override
    public final void close() {
        cacheService.destroy();
    }
}