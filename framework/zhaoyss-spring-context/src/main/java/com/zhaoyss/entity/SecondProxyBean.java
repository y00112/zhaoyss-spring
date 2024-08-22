package com.zhaoyss.entity;

public class SecondProxyBean extends OriginBean{
    final  OriginBean target;

    public SecondProxyBean(OriginBean target) {
        this.target = target;
    }

    @Override
    public String getName() {
        return target.getName();
    }
}
