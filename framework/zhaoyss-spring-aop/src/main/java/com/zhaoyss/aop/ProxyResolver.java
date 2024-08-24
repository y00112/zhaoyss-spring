package com.zhaoyss.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyResolver {


    private ProxyResolver() {
    }

    private static ProxyResolver INSTANCE = null;

    public static ProxyResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProxyResolver();
        }
        return INSTANCE;
    }

    // ByteBuddy实例
    ByteBuddy byteBuddy = new ByteBuddy();

    // 传入原始Bean、拦截器，返回代理后的实例
    public <T> T createProxy(T bean, InvocationHandler handler) {
        // 目标Bean的Class类型
        Class<?> targetClass = bean.getClass();
        // 动态创建Proxy的Class
        Class<?> proxyClass = this.byteBuddy
                // 子类用默认无参构造方法
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // 拦截所有public方法
                .method(ElementMatchers.isPublic())
                .intercept(InvocationHandlerAdapter.of(
                        // 新拦截器实例
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                // 将方法用代理至原始Bean
                                return handler.invoke(bean, method, args);
                            }
                        }
                ))
                // 生成字节码
                .make()
                // 加载字节码
                .load(targetClass.getClassLoader())
                .getLoaded();
        // 创建 Proxy 实例
        Object proxy;
        try {
            proxy = proxyClass.getConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }
}
