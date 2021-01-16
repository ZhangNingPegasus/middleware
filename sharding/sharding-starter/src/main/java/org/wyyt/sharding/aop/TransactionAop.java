package org.wyyt.sharding.aop;

import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wyyt.tool.anno.TranSave;

/**
 * XA configuration of ShardingSphere distributed transaction
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Aspect
public class TransactionAop {
    @Around(value = "@annotation(tranSave)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final TranSave tranSave) throws Throwable {
        TransactionTypeHolder.set(TransactionType.XA);
        try {
            return point.proceed();
        } finally {
            TransactionTypeHolder.clear();
        }
    }
}