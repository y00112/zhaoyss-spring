package com.zhaoyss.before;

import com.zhaoyss.annotation.Around;
import com.zhaoyss.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Around("logInvocationHandler")
public class OriginBean {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public String hello(String name){
        logger.info("Hello, {}.",name);
        return "Hello, " + name + ".";
    }

    public String morning(String name){
        logger.info("Morning, {}.",name);
        return "Morning, "+ name + ".";
    }

    /*
        15:21:18.577 [main] INFO  c.z.before.LogInvocationHandler -- [Before] hello()
        15:21:18.578 [main] INFO  com.zhaoyss.before.OriginBean -- Hello, Bob.
        15:21:18.578 [main] INFO  c.z.before.LogInvocationHandler -- [Before] morning()
        15:21:18.578 [main] INFO  com.zhaoyss.before.OriginBean -- Morning, Alice.
     */
}
