package com.zhaoyss.around;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class AroundApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor(){
        return new AroundProxyBeanPostProcessor();
    }
}
