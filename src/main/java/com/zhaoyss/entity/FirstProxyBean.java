package com.zhaoyss.entity;

public class FirstProxyBean extends OriginBean{
    final OriginBean target;

    public FirstProxyBean(OriginBean target) {
        this.target = target;
    }

    @Override
    public String getName() {
        return target.getName();
    }
}