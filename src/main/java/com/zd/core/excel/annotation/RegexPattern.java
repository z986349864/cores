package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegexPattern {

    String value();

    /**
     * 匹配模式：默认值
     * 忽略大小写：mode = Pattern.CASE_INSENSITIVE
     * @return
     */
    int mode() default 0;

    String message() default "formatter error";
}
