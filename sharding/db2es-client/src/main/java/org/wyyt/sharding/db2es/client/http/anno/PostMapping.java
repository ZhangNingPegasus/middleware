package org.wyyt.sharding.db2es.client.http.anno;

import java.lang.annotation.*;

/**
 * The annotation of rest controller for Handler
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
public @interface PostMapping {
    String value();
}