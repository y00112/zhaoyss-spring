package com.zhaoyss.ioc;

import com.zhaoyss.exception.BeanCreationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BeanDefinition implements Comparable<BeanDefinition>{

    // 全局唯一的Bean name
    String name;

    // Bean的声明类型
    Class<?> beanClass;

    // Bean的实例
    Object instance = null;

    // 构造方法/null
    Constructor<?> constructor;

    // 工厂方法名称/null
    String factoryName;

    // 工厂方法/null
    Method factoryMethod;

    // bean的顺序
    int order;

    // 是否标识 @Primary
    boolean primary;

    // init/destroy方法名称
    String initMethodName;
    String destroyMethodName;

    // init/destroy方法
    Method initMethod;
    Method destroyMethod;

    public BeanDefinition(String name, Class<?> beanClass, Constructor<?> constructor, int order, boolean primary, String initMethodName, String destroyMethodName, Method initMethod,Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = constructor;
        this.order = order;
        this.primary = primary;
        this.factoryName = null;
        this.factoryMethod = null;
        constructor.setAccessible(true);
        setInitAndDestoryMethod(initMethodName,destroyMethodName,initMethod,destroyMethod);
    }

    public BeanDefinition(String name, Class<?> beanClass, String factoryName, Method factoryMethod, int order, boolean primary, String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = null;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = order;
        this.primary = primary;
        factoryMethod.setAccessible(true);
        setInitAndDestoryMethod(initMethodName,destroyMethodName,initMethod,destroyMethod);
    }

    private void setInitAndDestoryMethod(String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.initMethodName = initMethodName;
        this.destroyMethodName =destroyMethodName;
        if (initMethod != null){
            initMethod.setAccessible(true);
        }
        if (destroyMethod != null){
            destroyMethod.setAccessible(true);
        }
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    public String getName() {
        return name;
    }

    public Object getRequiredInstance(){
        if (this.instance == null){
            throw new BeanCreationException(String.format("Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                    this.getName(), this.getBeanClass().getName()));
        }
        return this.instance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public Method getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    public Method getDestroyMethod() {
        return destroyMethod;
    }

    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "name='" + name + '\'' +
                ", beanClass=" + beanClass +
                ", instance=" + instance +
                ", constructor=" + constructor +
                ", factoryName='" + factoryName + '\'' +
                ", factoryMethod=" + factoryMethod +
                ", order=" + order +
                ", primary=" + primary +
                ", initMethodName='" + initMethodName + '\'' +
                ", destroyMethodName='" + destroyMethodName + '\'' +
                ", initMethod=" + initMethod +
                ", destroyMethod=" + destroyMethod +
                '}';
    }

    @Override
    public int compareTo(BeanDefinition def) {
        int cmp = Integer.compare(this.order, def.order);
        if (cmp != 0) {
            return cmp;
        }
        return this.name.compareTo(def.name);
    }
}
