package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Length {

    boolean notNull() default false;

    boolean notBlank() default false;

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "length error";
}
