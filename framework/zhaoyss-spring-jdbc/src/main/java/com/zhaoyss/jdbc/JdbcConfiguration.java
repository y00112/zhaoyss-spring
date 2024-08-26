package com.zhaoyss.jdbc;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.annotation.Value;
import com.zhaoyss.aop.TransactionalBeanPostProcessor;
import com.zhaoyss.jdbc.tx.DataSourceTransactionManager;
import com.zhaoyss.jdbc.tx.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class JdbcConfiguration {

    @Bean(destroyMethod = "close")
    DataSource dataSource(
            // properties:
            @Value("${zhaoyss.datasource.url}") String url,
            @Value("${zhaoyss.datasource.username}") String username,
            @Value("${zhaoyss.datasource.password}") String password,
            @Value("${zhaoyss.datasource.driver-class-name:}") String driver,
            @Value("${zhaoyss.datasource.maximum-pool-size:20}") int maximumPoolSize,
            @Value("${zhaoyss.datasource.minimum-pool-size:1}") int minimumPoolSize,
            @Value("${zhaoyss.datasource.connection-timeout:30000}") int connTimeout
    ) {
        var config = new HikariConfig();
        config.setAutoCommit(true);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driver != null) {
            config.setDriverClassName(driver);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    TransactionalBeanPostProcessor transactionalBeanPostProcessor() {
        return new TransactionalBeanPostProcessor();
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
