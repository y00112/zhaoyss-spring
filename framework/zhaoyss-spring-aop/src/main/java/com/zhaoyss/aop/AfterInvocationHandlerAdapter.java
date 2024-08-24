package com.zhaoyss.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class AfterInvocationHandlerAdapter implements InvocationHandler {

    // after 允许修改方法返回值
    public abstract Object after(Object proxy, Object returnValue, Method method,Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(method, args);
        return after(proxy,ret,method,args);
    }
}

