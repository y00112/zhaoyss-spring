package com.zhaoyss.web;

import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.annotation.Import;
import com.zhaoyss.jdbc.JdbcConfiguration;

@ComponentScan
@Configuration
@Import({JdbcConfiguration.class, WebMvcConfiguration.class})
public class HelloConfiguration {
}
