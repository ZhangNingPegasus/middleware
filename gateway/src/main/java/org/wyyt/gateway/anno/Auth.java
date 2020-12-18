package org.wyyt.gateway.anno;

import java.lang.annotation.*;

/**
 * [类描述信息]
 * <p>
 * *********************************************************
 * 姓名            事项               时间          动作
 * 张宁            创建                      创建
 * *********************************************************
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Auth {
}
