package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredBy {

    Type type() default Type.EXIST_ONE;

    /**
     * using this field while the type is EXIST_ONE or ALL_EXIST
     *
     * @return
     */
    String[] fields() default {};

    /**
     * using this field while the type is VALUE_IN
     *
     * @return
     */
    String field() default "";

    /**
     * using this field while the type is VALUE_IN
     *
     * @return
     */
    String[] values() default {};

    String message() default "required error";

    enum Type {
        EXIST_ONE,
        ALL_EXIST,
        VALUE_IN
    }
}
