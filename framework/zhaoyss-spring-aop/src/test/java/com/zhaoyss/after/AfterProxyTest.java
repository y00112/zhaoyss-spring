package com.zhaoyss.after;

import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AfterProxyTest {

    @Test
    public void testAfterProxy() {
        try (var ctx = new AnnotationConfigApplicationContext(AfterApplication.class, createPropertyResolver())) {
            AfterOriginBean proxy = ctx.getBean(AfterOriginBean.class);
            // should change return value:
            proxy.hello("Bob");
            proxy.morning("Alice");
            System.out.println();

        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        var pr = new PropertyResolver(ps);
        return pr;
    }
}
