package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataType {

    Type type();

    String message() default "type error";

    enum Type {
        /**
         * any type
         */
        ANY,
        /**
         * 1231
         */
        INTEGER,
        /**
         * 2313213
         */
        LONG,
        /**
         * 12.31
         */
        BIG_DECIMAL
    }
}
