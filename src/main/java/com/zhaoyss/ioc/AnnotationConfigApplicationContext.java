package com.zhaoyss.ioc;

import com.zhaoyss.exception.NoUniqueBeanDefinitionException;
import com.zhaoyss.io.PropertyResolver;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 保存所有的 bean
 */
public class AnnotationConfigApplicationContext {

    Map<String, BeanDefinition> beans;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver){
        // 扫描获取所有Bean的Class类型
        Set<String> beanClassNames = scanForClassNames(configClass);
        // 创建Bean的定义
        // this.beans = createBeanDefinitions(beanClassNames);
    }

    private Set<String> scanForClassNames(Class<?> configClass) {
        // 获取 @ComponentScan注解
        // ComponentScan scan = ClassUtils.findAnnotation(configClass,ComponScan)

        return null;
    }

    // 根据 Name 查找 BeanDefinition，如果 Name 不存在，返回 null
    @Nullable
    public BeanDefinition findBeanDefinition(String name) {
        return this.beans.get(name);
    }

    // 根据Type查找若干个BeanDefinition，返回0个或多个:
    List<BeanDefinition> findBeanDefinitions(Class<?> type){
        return this.beans.values().stream()
                .filter(def -> type.isAssignableFrom(def.getBeanClass()))
                .sorted().collect(Collectors.toList());
    }


    // 根据Type查找某个BeanDefinition，如果不存在返回null，如果存在多个返回 @Primary 标注的一个
    public BeanDefinition findBeanDefinition(Class<?> type){
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if (defs.isEmpty()){
            return null;
        }
        if (defs.size() == 1){
            return defs.get(0);
        }
        // 多于一个时，查找 @Primary
        List<BeanDefinition> primaryDefs = defs.stream().filter(def-> def.isPrimary()).collect(Collectors.toList());
        if (primaryDefs.size() ==1){
            return primaryDefs.get(0);
        }

        if (primaryDefs.isEmpty()){
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.",type.getName()));
        }else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.",type.getName()));
        }
    }
}

