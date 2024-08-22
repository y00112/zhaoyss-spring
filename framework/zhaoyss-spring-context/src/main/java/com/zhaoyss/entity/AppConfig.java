package com.zhaoyss.entity;

import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@ComponentScan
public class AppConfig {

    @Bean
    ZoneId createZoneId(){
        return ZoneId.of("Z");
    }

}
