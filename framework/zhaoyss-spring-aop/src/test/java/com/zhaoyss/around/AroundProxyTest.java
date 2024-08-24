package com.zhaoyss.around;

import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.io.PropertyResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class AroundProxyTest {

    @Test
    public void testAroundProxy(){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AroundApplication.class, createPropertyResolver());
        // 获得的是代理类
        OriginBean proxy = ctx.getBean(OriginBean.class);
        System.out.println(proxy.getClass().getName());

        // 断言不相同
        Assertions.assertNotSame(OriginBean.class,proxy.getClass());
        // 代理类的name找不到
        Assertions.assertNull(proxy.name);

        // aop
        System.out.println(proxy.hello());
        // 没有 aop
        System.out.println(proxy.morning());

        // 测试注入代理
        OtherBean other = ctx.getBean(OtherBean.class);
        // 注入的 origin的代理
        Assertions.assertSame(proxy, other.originBean);
        System.out.println(other.originBean.hello());

    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("customer.name", "Bob");
        var pr = new PropertyResolver(ps);
        return pr;
    }
}
