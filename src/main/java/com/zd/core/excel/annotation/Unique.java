package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

    boolean ignoreInFile() default false;

    String classReference() default "";

    String existMethod() default "";

    String fieldName();

    String message() default "duplicate value";
}
