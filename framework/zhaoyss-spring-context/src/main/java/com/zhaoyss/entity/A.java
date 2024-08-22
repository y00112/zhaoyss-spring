package com.zhaoyss.entity;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;
import jakarta.annotation.PostConstruct;

@Component
public class A {

    B b;

    C c;

    public A(@Autowired B b) {
        this.b = b;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public C getC() {
        return c;
    }

    @Autowired
    public void setC(C c) {
        this.c = c;
    }

    @PostConstruct
    void init() {
        System.out.println("执行了A的init方法");
    }


}
