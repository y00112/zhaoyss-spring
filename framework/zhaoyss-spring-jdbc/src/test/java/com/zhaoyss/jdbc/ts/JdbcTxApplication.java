package com.zhaoyss.jdbc.ts;

import com.zhaoyss.annotation.ComponentScan;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.annotation.Import;
import com.zhaoyss.jdbc.JdbcConfiguration;

@ComponentScan
@Configuration
@Import(JdbcConfiguration.class)
public class JdbcTxApplication {
}
