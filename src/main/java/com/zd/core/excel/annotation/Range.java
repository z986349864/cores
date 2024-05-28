package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Range {

    boolean notNull() default false;

    long min() default 0;

    long max() default Long.MAX_VALUE;

    String message() default "range error";
}
