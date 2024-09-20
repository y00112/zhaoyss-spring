package com.zhaoyss.web;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Bean;
import com.zhaoyss.annotation.Configuration;
import com.zhaoyss.annotation.Value;
import jakarta.servlet.ServletContext;

import java.util.Objects;

@Configuration
public class WebMvcConfiguration {

    private static ServletContext servletContext = null;

    public static void setServletContext(ServletContext ctx) {
        servletContext = ctx;
    }

    @Bean(initMethod = "init")
    ViewResolver viewResolver(
            @Autowired ServletContext servletContext,
            @Value("${zhaoyss.web.freemarker.template-path:/WEB-INF/templates}") String templatePath,
            @Value("${zhaoyss.web.freemarker.template-encoding:UTF-8}") String templateEncoding
    ) {
        return new FreeMarkerViewResolver(servletContext, templatePath, templateEncoding);
    }

    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
    }
}
