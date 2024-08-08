package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    /**
     * 必须
     */
    boolean value() default true;

    /**
     * Bean 的名称
     */
    String name() default "";
}
