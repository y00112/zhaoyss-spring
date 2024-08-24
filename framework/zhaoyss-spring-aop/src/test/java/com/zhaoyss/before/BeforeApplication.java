package com.zhaoyss.before;

import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class BeforeApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor(){
        return new AroundProxyBeanPostProcessor();
    }
}
