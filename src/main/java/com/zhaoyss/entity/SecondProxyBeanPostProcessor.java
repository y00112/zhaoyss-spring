package com.zhaoyss.entity;

import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Order;
import com.zhaoyss.ioc.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(200)
public class SecondProxyBeanPostProcessor implements BeanPostProcessor {
    // 保存原始 Bean
    Map<String,Object> originBeans = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (OriginBean.class.isAssignableFrom(bean.getClass())){
            // 检测到 OriginBean，创建SecondProxy:
            var proxy = new SecondProxyBean((OriginBean) bean);
            // 保存原始Bean
            originBeans.put(beanName,bean);
            // 返回proxy
            return proxy;
        }
        return bean;
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originBeans.get(beanName);
        if (origin != null){
            // 存在原始Bean时，返回原始Bean
            return origin;
        }
        return bean;
    }
}
