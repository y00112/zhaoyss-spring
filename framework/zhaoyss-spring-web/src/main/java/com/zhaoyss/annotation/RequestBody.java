package com.zhaoyss.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER) // ElementType.PARAMETER 方法参数
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
}
