package org.wyyt.springcloud.gateway.entity.anno;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * The annotation of transactional for read only
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Transactional(readOnly = true, timeout = 30)
public @interface TranRead {
}