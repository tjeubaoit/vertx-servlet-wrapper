package com.admicro.vertx.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface VertxletRequestMapping {

    String[] url() default {};
    int loadOnStartup() default 100;
    boolean usingDatabase() default false;
}