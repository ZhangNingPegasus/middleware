package org.wyyt.sharding.interceptor.plugin;

import org.apache.ibatis.plugin.Invocation;

/**
 * interface of the mybatis interceptor
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public interface MybatisInterceptor {
    void setParameter(final String name, final Object value);

    <T> T getParameter(final String name);

    void clearParameter();

    Object[] before(final Invocation invocation, final Object[] variables) throws Exception;

    Object[] success(final Invocation invocation, Object result, final Object[] variables) throws Exception;

    Object[] failure(final Throwable e, final Invocation invocation, final Object[] variables) throws Exception;

    Object[] complete(final Invocation invocation, final Object[] variables) throws Exception;

    boolean enabled(final Invocation invocation) throws Exception;

    int order();
}