/***********************************************************************************
 * Copyright (c) 2013. Nickolay Gerilovich. Russia.
 *   Some Rights Reserved.
 ************************************************************************************/

package com.github.nickvl.xspring.core.log.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Logging
public @interface LogException {

    Exc[] fatal() default {};


    Exc[] value() default @Exc(value = Exception.class, stacktrace = true);


    Exc[] warn() default {};


    Exc[] info() default {};


    Exc[] debug() default {};


    Exc[] trace() default {};


    public @interface Exc {

        Class<? extends Exception>[] value();


        boolean stacktrace() default false;
    }

}