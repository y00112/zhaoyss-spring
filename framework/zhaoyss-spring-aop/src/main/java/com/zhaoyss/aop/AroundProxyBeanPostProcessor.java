package com.zhaoyss.aop;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.content.ApplicationContextUtils;
import com.zhaoyss.content.BeanDefinition;
import com.zhaoyss.content.BeanPostProcessor;
import com.zhaoyss.content.ConfigurableApplicationContext;
import com.zhaoyss.exception.AopConfigException;
import com.zhaoyss.io.PropertyResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

// 检测每个Bean实例是否带有@Around注解，如果有就根据注解的值查找Bean作为 InvocationHandler，最后创建Proxy。
// 返回前保存了原始Bean的引用，因为IOC容器在后续的注入阶段要把相关依赖和值注入到原始Bean
public class AroundProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Around> {


}
