package org.wyyt.admin.ui.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ComponentRegistor.class)
public @interface EnableAdminUI {
}