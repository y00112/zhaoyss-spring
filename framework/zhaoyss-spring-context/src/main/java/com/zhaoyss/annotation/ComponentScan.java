package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScan {

    /**
     * 要扫描的包名，默认是当前包
     */
    String[] value() default {};
}
