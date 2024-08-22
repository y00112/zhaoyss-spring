package com.zhaoyss;

import com.zhaoyss.entity.AppConfig;
import com.zhaoyss.entity.InjectProxyOnConstructorBean;
import com.zhaoyss.entity.OriginBean;
import com.zhaoyss.entity.SecondProxyBean;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.content.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IocTest {

    @Test
    void main(){
        // 验证 ResourceResolver
        // ResourceResolver resourceResolver = new ResourceResolver("com.zhaoyss");
        // resourceResolver.scan().forEach(aClass -> {
        //     System.out.println(aClass.getName());
        // });
        //
        // 验证 PropertyResolver
        // Map<String, Object> configs = YamlUtils.loadYamlAsPlainMap("/application.yaml");
        // Properties props = new Properties();
        // props.putAll(configs);
        // PropertyResolver pr = new PropertyResolver(props);
        // String property = pr.getProperty("${app.version:1}", String.class);
        // System.out.println(property);
        //

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class, createPropertyResolver());

        // 获取OriginBean的实例，此处获取应该是 SendProxyBeanProxy
        OriginBean proxy = ctx.getBean(OriginBean.class);
        Assertions.assertSame(SecondProxyBean.class,proxy.getClass());

        // proxy的name和version字段并没有被注入
        Assertions.assertNull(proxy.name);
        Assertions.assertNull(proxy.version);

        // 但是调用 proxy 的 getName() 会最终调用原始Bean的getName()从而正确返回
        Assertions.assertEquals("Scan App",proxy.getName());

        // 获取InjectProxyOnConstructorBean 实例
        var inject = ctx.getBean(InjectProxyOnConstructorBean.class);
        // 注入的OriginBean应该为 Proxy，而且和前面返回的proxy是同一实例
        Assertions.assertSame(proxy,inject.injected);
    }


    static PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("jdbc.url", "jdbc:hsqldb:file:testdb.tmp");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        ps.put("convert.boolean", "true");
        ps.put("convert.byte", "123");
        ps.put("convert.short", "12345");
        ps.put("convert.integer", "1234567");
        ps.put("convert.long", "123456789000");
        ps.put("convert.float", "12345.6789");
        ps.put("convert.double", "123456789.87654321");
        ps.put("convert.localdate", "2023-03-29");
        ps.put("convert.localtime", "20:45:01");
        ps.put("convert.localdatetime", "2023-03-29T20:45:01");
        ps.put("convert.zoneddatetime", "2023-03-29T20:45:01+08:00[Asia/Shanghai]");
        ps.put("convert.duration", "P2DT3H4M");
        ps.put("convert.zoneid", "Asia/Shanghai");
        var pr = new PropertyResolver(ps);
        return pr;
    }


}
