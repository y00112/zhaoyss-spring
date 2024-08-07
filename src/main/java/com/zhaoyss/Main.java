package com.zhaoyss;

import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.utils.YamlUtils;

import java.util.Map;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // ResourceResolver resourceResolver = new ResourceResolver("com.zhaoyss");
        // resourceResolver.scan().forEach(aClass -> {
        //     System.out.println(aClass.getName());
        // });

        Map<String, Object> configs = YamlUtils.loadYamlAsPlainMap("/application.yaml");
        Properties props = new Properties();
        props.putAll(configs);
        PropertyResolver pr = new PropertyResolver(props);
        String property = pr.getProperty("${app.version:1}", String.class);
        System.out.println(property);
    }
}