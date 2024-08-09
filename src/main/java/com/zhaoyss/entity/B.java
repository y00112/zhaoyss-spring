package com.zhaoyss.entity;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;

@Component
public class B {

    C c;

    public B(@Autowired C c) {
        this.c = c;
    }
}
