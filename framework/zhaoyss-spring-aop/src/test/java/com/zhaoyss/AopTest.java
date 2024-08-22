package com.zhaoyss;

import com.zhaoyss.aop.ProxyResolver;
import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.entity.OriginBean;
import com.zhaoyss.handler.PoliteInvocationHandler;
import com.zhaoyss.io.PropertyResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class AopTest {


    @Test
    void main(){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AopTest.class, createPropertyResolver());
        OriginBean originBean = new OriginBean();
        originBean.name = "Bob";
        // 原始Bean的hello
        Assertions.assertEquals("Hello,Bob.",originBean.hello());
        // 创建Proxy
        OriginBean proxy = new ProxyResolver().createProxy(originBean, new PoliteInvocationHandler());
        System.out.println(proxy.getClass().getName());
        Assertions.assertNull(proxy.name);
        // 调用带 @Polite的方法
        Assertions.assertEquals("Hello,Bob!",proxy.hello());
        // 调用不带 @Polite的方法
        System.out.println(proxy.morning());
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        var pr = new PropertyResolver(ps);
        return pr;
    }
}
