package com.zhaoyss.before;

import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class BeforeProxyTest {

    @Test
    public void testBeforeProxy(){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BeforeApplication.class, createPropertyResolver());
        OriginBean proxy = ctx.getBean(OriginBean.class);
        proxy.hello("Bob");
        proxy.morning("Alice");
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        var pr = new PropertyResolver(ps);
        return pr;
    }
}
