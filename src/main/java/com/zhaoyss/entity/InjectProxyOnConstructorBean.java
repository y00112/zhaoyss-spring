package com.zhaoyss.entity;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;

@Component
public class InjectProxyOnConstructorBean {
    public final OriginBean injected;

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
