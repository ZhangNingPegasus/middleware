package org.wyyt.sharding.cache.aop;


import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wyyt.sharding.cache.EhcacheService;
import org.wyyt.sharding.cache.anno.EhCache;

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
public final class EhCacheAop {
    private final EhcacheService ehcacheService;

    public EhCacheAop(EhcacheService ehcacheService) {
        this.ehcacheService = ehcacheService;
    }

    @Around(value = "@annotation(ehCache)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final EhCache ehCache) throws Throwable {
        final String typeName = point.getSignature().getDeclaringTypeName();
        final String methodName = point.getSignature().getName();
        final List<Object> argsList = Arrays.asList(point.getArgs());
        argsList.sort(Comparator.comparing(Object::toString));
        final String key = String.format("%s_%s_%s", typeName, methodName, StringUtils.join(argsList, "&"));
        Object result = this.ehcacheService.get(key);
        if (null == result) {
            result = point.proceed();
            this.ehcacheService.set(key, result);
        }
        return result;
    }
}