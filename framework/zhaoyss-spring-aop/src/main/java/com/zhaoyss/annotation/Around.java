package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Around {

    /**
     * Invocation handler bean name.
     */
    String value();
}
