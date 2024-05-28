package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Exist {

    String classReference();

    String existMethod();

    String fieldName();

    String message() default "duplicate value";
}
