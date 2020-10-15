package org.wyyt.db2es.client.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.Closeable;

/**
 * the wapper class of cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class CacheWrapper implements Closeable {
    private final Cache<String, Object> cache;

    public CacheWrapper() {
        this.cache = CacheBuilder.newBuilder().build();
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

    @Override
    public final void close() {
        if (null != this.cache) {
            this.cache.invalidateAll();
            this.cache.cleanUp();
        }
    }
}