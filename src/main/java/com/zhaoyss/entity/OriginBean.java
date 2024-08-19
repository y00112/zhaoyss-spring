package com.zhaoyss.entity;

import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Value;

@Component
public class OriginBean {

    @Value("${app.title}")
    public String name;

    @Value("${app.version}")
    public String version;

    public String getName(){
        return name;
    }
}
