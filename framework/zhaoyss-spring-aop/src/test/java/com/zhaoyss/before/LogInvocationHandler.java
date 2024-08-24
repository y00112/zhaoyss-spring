package com.zhaoyss.before;

import com.zhaoyss.annotation.Component;
import com.zhaoyss.aop.BeforeInvocationHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Component
public class LogInvocationHandler extends BeforeInvocationHandlerAdapter {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object poxy, Method method, Object[] args) {
        logger.info("[Before] {}()", method.getName());
    }
}
