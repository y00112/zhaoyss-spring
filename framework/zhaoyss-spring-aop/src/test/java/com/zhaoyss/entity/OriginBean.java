package com.zhaoyss.entity;

import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Polite;

@Component
public class OriginBean {

    public String name;


    @Polite
    public String hello(){
        return "Hello," + name + ".";
    }

    public String morning(){
        return "Morning," + name + ".";
    }
}
