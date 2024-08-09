package com.zhaoyss.entity;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;

@Component
public class A {

    B b;

    C c;

    public A(@Autowired B b, @Autowired C c) {
        this.b = b;
        this.c = c;
    }

}
