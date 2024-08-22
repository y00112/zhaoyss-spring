package com.zhaoyss.aop;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.content.BeanPostProcessor;
import com.zhaoyss.exception.AopConfigException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

// TODO:
public class AroundProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {
    Map<String,Object> originBeans = new HashMap<>();

    Class<A> annotationClass;

    public Object postProcessBeforeInitialization(Object bean, String beanName){
        Class<?> beanClass = bean.getClass();
        // 检测 @Around 注解
        Around anno = beanClass.getAnnotation(Around.class);
        if (anno != null){
            String handlerName;
            try {
                handlerName = (String) anno.annotationType().getMethod("value").invoke(anno);
            } catch (ReflectiveOperationException  e) {
                throw new AopConfigException(
                        String.format("@%s must have value() returned String type.",this.annotationClass.getSimpleName(),e)
                );
            }
        }
        return null;
    }
}
