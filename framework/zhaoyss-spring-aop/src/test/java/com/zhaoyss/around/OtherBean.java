package com.zhaoyss.around;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Order;

@Order(0)
@Component
public class OtherBean {

    public OriginBean originBean;

    public OtherBean(@Autowired OriginBean originBean){
        this.originBean = originBean;
    }
}
