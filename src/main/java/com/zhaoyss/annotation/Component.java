package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    /**
     * Bean 名称，默认带有首字母小写的简单类名
     */
    String value() default "";
}
