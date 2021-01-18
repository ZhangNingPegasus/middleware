package org.wyyt.sharding.cache.aop;


import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wyyt.sharding.cache.anno.LocalCache;
import org.wyyt.tool.cache.CacheService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * AOP used for cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Aspect
public final class LocalCacheAop {
    private final CacheService cacheService;

    public LocalCacheAop(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Around(value = "@annotation(ehCache)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final LocalCache ehCache) throws Throwable {
        final String typeName = point.getSignature().getDeclaringTypeName();
        final String methodName = point.getSignature().getName();
        final List<Object> argsList = Arrays.asList(point.getArgs());
        argsList.sort(Comparator.comparing(Object::toString));
        final String key = String.format("%s_%s_%s", typeName, methodName, StringUtils.join(argsList, "&"));
        Object result = this.cacheService.get(key);
        if (null == result) {
            result = point.proceed();
            this.cacheService.put(key, result);
        }
        return result;
    }
}