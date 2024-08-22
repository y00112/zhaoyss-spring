package com.zhaoyss.entity;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;
import jakarta.annotation.PreDestroy;

@Component
public class B {

    C c;

    public B(@Autowired C c) {
        this.c = c;
    }

    @PreDestroy
    void destroy() {
        System.out.println("B 的 destroy 方法");;
    }
}
