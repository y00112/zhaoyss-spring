package com.zhaoyss.around;


import com.zhaoyss.annotation.Around;
import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Polite;
import com.zhaoyss.annotation.Value;

@Component
@Around("aroundInvocationHandler")
public class OriginBean {

    @Value("${customer.name}")
    public String name;


    @Polite
    public String hello(){
        return "Hello," + name + ".";
    }

    public String morning(){
        return "Morning," + name + ".";
    }
}
