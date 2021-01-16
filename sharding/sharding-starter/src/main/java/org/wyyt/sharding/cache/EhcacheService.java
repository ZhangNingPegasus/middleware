package org.wyyt.sharding.cache;

import org.ehcache.Cache;
import org.springframework.beans.factory.DisposableBean;

/**
 * The service of Ehcache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class EhcacheService implements DisposableBean {
    private final Cache<String, Object> cache;

    public EhcacheService(final Cache<String, Object> cache) {
        this.cache = cache;
    }

    public final void set(final String name,
                          final Object value) {
        if (null != value) {
            this.cache.put(name, value);
        }
    }

    public final <T> T get(final String name) {
        Object result = this.cache.get(name);
        if (null == result) {
            return null;
        }
        return (T) result;
    }

    public final void remove(final String name) {
        this.cache.remove(name);
    }

    public final boolean containsKey(final String name) {
        return this.cache.containsKey(name);
    }

    @Override
    public final void destroy() {
        this.cache.clear();
    }
}