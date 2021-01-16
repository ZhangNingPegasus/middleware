package org.wyyt.sharding.context;

import org.springframework.beans.factory.InitializingBean;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.service.ShardingService;

/**
 * the thread-local of storing the current used data source.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class DbContext implements InitializingBean {
    private static ThreadLocal<String> threadLocalDbContext;
    private static ShardingService shardingService;

    public DbContext(final ShardingService shardingService) {
        DbContext.shardingService = shardingService;
    }

    public static String get() {
        return threadLocalDbContext.get();
    }

    public static void set(final String dimension) {
        threadLocalDbContext.set(dimension);
    }

    public static void clear() {
        threadLocalDbContext.remove();
        threadLocalDbContext.set(getPrimaryDimensionName());
    }

    @Override
    public final void afterPropertiesSet() {
        threadLocalDbContext = ThreadLocal.withInitial(DbContext::getPrimaryDimensionName);
    }

    private static String getPrimaryDimensionName() {
        final DimensionProperty primaryDimension = shardingService.getPrimaryDimension();
        if (null != primaryDimension) {
            return primaryDimension.getName();
        }
        return null;
    }
}