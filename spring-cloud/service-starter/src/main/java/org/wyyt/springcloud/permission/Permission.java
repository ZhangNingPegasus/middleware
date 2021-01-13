package org.wyyt.springcloud.permission;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Permission {
    /**
     * 权限名字，全局唯一，英文格式
     *
     * @return String
     */
    String name();

    /**
     * 权限描述
     *
     * @return String
     */
    String description() default "";
}