package org.wyyt.admin.ui.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * The annotation which enable the Admin UI
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ComponentRegistor.class)
public @interface EnableAdminUI {
}