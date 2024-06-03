package com.zd.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCache {
    boolean use() default true;

    String flush() default "";

    int time() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String key() default "";

}