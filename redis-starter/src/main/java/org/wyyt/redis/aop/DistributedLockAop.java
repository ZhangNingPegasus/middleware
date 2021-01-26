package org.wyyt.redis.aop;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wyyt.redis.anno.DistributedLock;
import org.wyyt.redis.service.RedisService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * AOP used for redis's distributed lock
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Aspect
public class DistributedLockAop {
    private final RedisService redisService;

    public DistributedLockAop(final RedisService redisService) {
        this.redisService = redisService;
    }

    @Around(value = "@annotation(distributedLock)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final DistributedLock distributedLock) throws Throwable {
        final String typeName = point.getSignature().getDeclaringTypeName();
        final String methodName = point.getSignature().getName();
        final List<Object> argsList = Arrays.asList(point.getArgs());
        argsList.sort(Comparator.comparing(Object::toString));
        final String key = String.format("%s_%s_%s", typeName, methodName, StringUtils.join(argsList, "&"));
        try (final RedisService.Lock lock = this.redisService.getDistributedLock(key)) {
            if (lock.hasLock()) {
                return point.proceed();
            } else {
                throw new RuntimeException(String.format("分布式锁[%s]获取失败", key));
            }
        }
    }
}