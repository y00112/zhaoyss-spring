package com.zhaoyss.handler;

import com.zhaoyss.annotation.Polite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PoliteInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        boolean isPoliteAnnotation = method.getAnnotation(Polite.class) != null;
        if (isPoliteAnnotation){
            String ret = (String) method.invoke(bean, args);
            if (ret.endsWith(".")){
                ret = ret.substring(0,ret.length()-1) + "!";
            }
            return ret;
        }
        return method.invoke(bean,args);
    }
}
