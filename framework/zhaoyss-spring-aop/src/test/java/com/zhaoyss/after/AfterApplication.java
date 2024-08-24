package com.zhaoyss.after;

import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class AfterApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor(){
        return new AroundProxyBeanPostProcessor();
    }
}
