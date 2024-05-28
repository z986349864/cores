package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface In {

    String[] value() default {};

    boolean i18nValue() default true;

    String i18nCode() default "";

    String message() default "illegal value";
}
