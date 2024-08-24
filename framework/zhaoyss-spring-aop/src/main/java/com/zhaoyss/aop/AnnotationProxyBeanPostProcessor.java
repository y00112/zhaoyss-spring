package com.zhaoyss.aop;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.content.ApplicationContextUtils;
import com.zhaoyss.content.BeanDefinition;
import com.zhaoyss.content.BeanPostProcessor;
import com.zhaoyss.content.ConfigurableApplicationContext;
import com.zhaoyss.exception.AopConfigException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class AnnotationProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {

    Map<String, Object> originBeans = new HashMap<>();

    Class<A> annotationClass;

    public AnnotationProxyBeanPostProcessor(){
        this.annotationClass = getParameterizedType();
    }

    @SuppressWarnings("unchecked")
    private Class<A> getParameterizedType() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            return (Class<A>) parameterizedType.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Class does not have a parameterized type");

    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        // 检测注解
        A anno = beanClass.getAnnotation(annotationClass);
        if (anno != null) {
            String handlerName;
            try {
                handlerName = (String) anno.annotationType().getMethod("value").invoke(anno);
            } catch (ReflectiveOperationException e) {
                throw new AopConfigException(
                        String.format("@%s must have value() returned String type.", this.annotationClass.getSimpleName(), e)
                );
            }
            Object proxy = createProxy(beanClass, bean, handlerName);
            originBeans.put(beanName, bean);
            return proxy;
        } else {
            return bean;
        }
    }

    Object createProxy(Class<?> beanClass, Object bean, String handlerName) {
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextUtils.getApplicationContext();
        BeanDefinition def = ctx.findBeanDefinition(handlerName);
        if (def == null) {
            throw new AopConfigException();
        }
        Object handlerBean = def.getInstance();
        if (handlerBean == null) {
            handlerBean = ctx.createBeanAsEarlySingleton(def);
        }
        if (handlerBean instanceof InvocationHandler handler) {
            return ProxyResolver.getInstance().createProxy(bean, handler);
        } else {
            throw new AopConfigException();
        }
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = this.originBeans.get(beanName);
        return origin != null ? origin : bean;
    }
}
