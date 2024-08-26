package com.zhaoyss.after;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.annotation.Component;

@Component
@Around("politeInvocationHandler")
public class AfterOriginBean {

    public String hello(String name) {
        return "Hello,Word.";
    }

    public String morning(String name) {
        return "Hello,Word.";
    }
}
