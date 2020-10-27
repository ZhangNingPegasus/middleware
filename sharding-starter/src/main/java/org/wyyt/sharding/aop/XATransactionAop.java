package org.wyyt.sharding.aop;

import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wyyt.sharding.anno.TranSave;

/**
 * XA configuration of ShardingSphere distributed transaction
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Aspect
public class XATransactionAop {
    @Around(value = "@annotation(tranSave)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final TranSave tranSave) throws Throwable {
        TransactionTypeHolder.set(tranSave.transactionType());
        try {
            return point.proceed();
        } finally {
            TransactionTypeHolder.clear();
        }
    }
}