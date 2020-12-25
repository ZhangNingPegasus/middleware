package org.wyyt.kafka.monitor.anno;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * The annotation of transactional for insert, update and delete
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Transactional(rollbackFor = Exception.class, timeout = 30)
public @interface TranSave {
}