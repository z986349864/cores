package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {

    /**
     * mapping to a excel column
     *
     * @return
     */
    String name() default "";

    /**
     * Dynamic assignment
     *
     * @return
     */
    int columnNo() default -1;

    String comment() default "";
}
