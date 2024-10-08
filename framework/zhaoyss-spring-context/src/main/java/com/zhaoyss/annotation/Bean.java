package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    String value() default "";

    String initMethod() default "";

    String destroyMethod() default "";
}
