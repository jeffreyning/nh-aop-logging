package com.github.nickvl.xspring.core.log.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName:
 * @Description: 业务模块标识
 * @Author: ninghao
 * @Date: 2020/11
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogModule {
    String value() default "";
}
