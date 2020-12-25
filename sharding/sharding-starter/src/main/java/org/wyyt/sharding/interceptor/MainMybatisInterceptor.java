package org.wyyt.sharding.interceptor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.ApplicationContext;
import org.wyyt.sharding.interceptor.plugin.MybatisInterceptor;

import java.util.*;

/**
 * The mybatis interceptor
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
public final class MainMybatisInterceptor implements Interceptor {
    private final ApplicationContext applicationContext;

    public MainMybatisInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public final Object intercept(final Invocation invocation) throws Throwable {
        final Map<String, MybatisInterceptor> pluginMap = this.applicationContext.getBeansOfType(MybatisInterceptor.class);
        if (pluginMap.isEmpty()) {
            return invocation.proceed();
        }

        final List<MybatisInterceptor> interceptorList = new ArrayList<>(pluginMap.values());
        interceptorList.sort(Comparator.comparingInt(MybatisInterceptor::order));

        //before
        Object[] parametersPrev = null;
        for (MybatisInterceptor interceptor : interceptorList) {
            if (!interceptor.enabled(invocation)) {
                continue;
            }
            parametersPrev = interceptor.before(invocation, parametersPrev);
        }

        try {
            final Object result = invocation.proceed();
            //success
            parametersPrev = null;
            for (int i = interceptorList.size() - 1; i >= 0; i--) {
                final MybatisInterceptor interceptor = interceptorList.get(i);
                if (!interceptor.enabled(invocation)) {
                    continue;
                }
                parametersPrev = interceptor.success(invocation, result, parametersPrev);
            }
            return result;
        } catch (final Throwable e) {
            //failure
            parametersPrev = null;
            for (final MybatisInterceptor interceptor : interceptorList) {
                if (!interceptor.enabled(invocation)) {
                    continue;
                }
                parametersPrev = interceptor.failure(e, invocation, parametersPrev);
            }
            throw e;
        } finally {
            //complete
            parametersPrev = null;
            for (int i = interceptorList.size() - 1; i >= 0; i--) {
                final MybatisInterceptor interceptor = interceptorList.get(i);
                if (!interceptor.enabled(invocation)) {
                    continue;
                }
                parametersPrev = interceptor.complete(invocation, parametersPrev);
            }
        }
    }

    @Override
    public final Object plugin(final Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public final void setProperties(final Properties properties) {
    }
}