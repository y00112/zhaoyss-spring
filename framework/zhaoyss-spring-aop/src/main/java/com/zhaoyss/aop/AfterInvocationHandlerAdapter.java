package com.zhaoyss.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class AfterInvocationHandlerAdapter implements InvocationHandler {

    // after 允许修改方法返回值
    public abstract Object after(Object proxy, Object returnValue, Method method, Object[] args);

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Ensure the target object is the proxy itself or a compatible type
        Object result = method.invoke(proxy, args);
        return after(proxy, result, method, args);
    }
}

