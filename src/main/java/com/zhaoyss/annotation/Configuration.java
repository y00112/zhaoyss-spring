package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

    /**
     * Bean 名称
     */
    String value() default "";
}
