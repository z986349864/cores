package com.zd.core.excel.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelData {

    /**
     * Is name an internationalized code
     *
     * @return
     */
    boolean i18nHeader() default true;

    String[] languages() default {"en-US", "zh-CN", "de-DE"};
}
