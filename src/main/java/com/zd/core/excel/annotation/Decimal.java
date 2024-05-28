package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decimal {

    boolean notNull() default false;

    long min() default 0;

    long max() default Long.MAX_VALUE;

    int precision() default 8;

    int scale() default 2;

    String message() default "BigDecimal error";
}
