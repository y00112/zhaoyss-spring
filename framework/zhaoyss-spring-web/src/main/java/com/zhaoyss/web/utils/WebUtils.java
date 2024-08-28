package com.zhaoyss.web.utils;

import com.zhaoyss.content.ApplicationContextUtils;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.utils.ClassPathUtils;
import com.zhaoyss.utils.YamlUtils;
import com.zhaoyss.web.DispatcherServlet;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;

public class WebUtils {
    public static final String DEFAULT_PARAM_VALUE = "\0\t\0\t\0";
    static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    static final String CONFIG_APP_YAML = "/application.yaml";
    static final String CONFIG_APP_PROP = "/application.properties";

    public static PropertyResolver createPropertyResolver(){
        final Properties props = new Properties();

        try {
            Map<String,Object> yamlMap = YamlUtils.loadYamlAsPlainMap(CONFIG_APP_YAML);
            logger.info("load config: {}",CONFIG_APP_YAML);
            for (String key : yamlMap.keySet()) {
                Object value = yamlMap.get(key);
                if (value instanceof String strValue){
                    props.put(key,strValue);
                }
            }
        }catch (UncheckedIOException e){
            if (e.getCause() instanceof FileNotFoundException){
                // 尝试加载 application.properties
                ClassPathUtils.readInputStream(CONFIG_APP_PROP,(input)->{
                    logger.info("load config: {}",CONFIG_APP_PROP);
                    props.load(input);
                    return true;
                });
            }
        }
        return new PropertyResolver(props);
    }

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(ApplicationContextUtils.getRequiredApplicationContext(), propertyResolver);
        logger.info("register servlet {} for URL '/' ",dispatcherServlet.getClass().getName());
        var dispatcherReg = servletContext.addServlet("dispatcherServlet",dispatcherServlet);
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);

    }
}
