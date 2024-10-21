package com.zhaoyss.web.service;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DbInitializer {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @PostConstruct
    void init(){
        logger.info("init databases...");
        userService.initDb();
    }
}
