package com.zhaoyss.web;

import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.content.ApplicationContext;
import com.zhaoyss.exception.NestedRuntimeException;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.web.utils.WebUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextLoaderListener implements ServletContextListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    // Servlet 容器启动时自动调用
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("init {}.",getClass().getName());
        var servletContext = sce.getServletContext();
        var propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProperty("${zhaoyss.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        // 创建ioc容器
        var applicationContext = createApplicationContext(servletContext.getInitParameter("configuration"),propertyResolver);
        // 实例化DispatcherServlet
        WebUtils.registerDispatcherServlet(servletContext,propertyResolver);

        // 注册DispatcherServlet
    }

    private ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) {
        logger.info("init ApplicationContext by configuration: {}", configClassName);
        if (configClassName == null || configClassName.isEmpty()){
            throw new NestedRuntimeException("Cannot init ApplicationContext for missing init param name: configuration.");
        }
        Class<?> cofigClass;
        try {
            cofigClass = Class.forName(configClassName);
        }catch (ClassNotFoundException e){
            throw new NestedRuntimeException("Cloud not load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplicationContext(cofigClass,propertyResolver);
    }
}
