package com.zhaoyss.ioc;

import com.zhaoyss.annotation.*;
import com.zhaoyss.exception.BeanDefinitionException;
import com.zhaoyss.exception.NoUniqueBeanDefinitionException;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.io.ResourceResolver;
import com.zhaoyss.utils.ClassUtils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 保存所有的 bean
 */
public class AnnotationConfigApplicationContext {

    final Logger logger = LoggerFactory.getLogger(getClass());

    Map<String, BeanDefinition> beans;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) {
        // 扫描获取所有Bean的Class类型
        Set<String> beanClassNames = scanForClassNames(configClass);
        // 装配 BeanDefinition
        this.beans = createBeanDefinitions(beanClassNames);



    }

    private Map<String, BeanDefinition> createBeanDefinitions(Set<String> classNameSet) {
        Map<String, BeanDefinition> defs = new HashMap<>();
        for (String className : classNameSet) {
            // 获取Class
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            // 是否标注 @Component ?
            Component component = ClassUtils.findAnnotation(clazz, Component.class);
            if (component != null) {
                // 获取 Bean 的名称
                String beanName = ClassUtils.getBeanName(clazz);
                var def = new BeanDefinition();
                def.setName(beanName);
                def.setBeanClass(clazz);
                def.setConstructor(getSuitableConstructor(clazz));
                def.setOrder(getOrder(clazz));
                def.setPrimary(clazz.isAnnotationPresent(Primary.class));
                // init/destroy 方法名称
                def.setInitMethodName(null);
                def.setDestroyMethodName(null);
                // 查找 PostConstruct 方法
                def.setInitMethod(ClassUtils.findAnnotationMethod(clazz, PostConstruct.class));
                def.setDestroyMethod(ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinitions(defs, def);
                // 查找是否有 @configuration:
                Configuration configuration = ClassUtils.findAnnotation(clazz, Configuration.class);
                if (configuration != null) {
                    // 查找 @Bean 方法
                    scanFactoryMethods(beanName, clazz, defs);
                }
            }
        }
        return defs;
    }

    /**
     * 扫描带有 @Bean 的工厂方法
     *
     * <code>
     * @Configuration
     * public class AppConfiguration{
     *
     *      @Bean
     *      ZoneId createZone(){
     *          return ZoneId.of("Z");
     *      }
     * }
     * </code>
     */
    private void scanFactoryMethods(String factoryBeanName, Class<?> clazz, Map<String, BeanDefinition> defs) {
        for (Method method : clazz.getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be private.");
                }
                Class<?> beanClass = method.getReturnType();
                if (beanClass.isPrimitive()) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return primitive type.");
                }
                if (beanClass == void.class || beanClass == Void.class) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return void.");
                }

                var def = new BeanDefinition();
                def.setName(ClassUtils.getBeanName(method));
                def.setBeanClass(beanClass);
                def.setFactoryName(factoryBeanName);
                def.setFactoryMethod(method);
                def.setOrder(getOrder(method));
                def.setPrimary(method.isAnnotationPresent(Primary.class));
                // init/destroy 方法名称
                def.setInitMethodName(bean.initMethod().isEmpty() ? null : bean.initMethod());
                def.setDestroyMethodName(bean.destroyMethod().isEmpty() ? null : bean.destroyMethod());
                // 查找 PostConstruct 方法
                def.setInitMethod(null);
                def.setDestroyMethod(null);
                addBeanDefinitions(defs, def);
            }
        }
    }

    /**
     * 检查并添加 BeanDefinition
     */
    private void addBeanDefinitions(Map<String, BeanDefinition> defs, BeanDefinition def) {
        logger.atDebug().log("define bean: {}", def);
        if (defs.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    private int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    public int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    /**
     * 获取 public Constructor 或者 no-public constructor
     */
    private Constructor<?> getSuitableConstructor(Class<?> clazz) {
        Constructor<?>[] cons = clazz.getConstructors();
        if (cons.length == 0) {
            cons = clazz.getDeclaredConstructors();
            if (cons.length != 1) {
                throw new BeanDefinitionException("More that one constructor found in class " + clazz.getName() + ".");
            }
        }
        if (cons.length != 1) {
            throw new BeanDefinitionException("More than one public constructor found in class " + clazz.getName() + ".");
        }
        return cons[0];
    }

    /**
     * 扫描指定包的所有Class名称，以及通过 @Import 导入的Class名称
     */
    private Set<String> scanForClassNames(Class<?> configClass) {
        // 获取 @ComponentScan注解
        ComponentScan scan = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        // 获取注解配置的 package 名字，未配置则默认当前类所在的包
        String[] scanPackages = scan == null || scan.value().length == 0 ? new String[]{configClass.getPackage().getName()} : scan.value();
        Set<String> classNameSet = new HashSet<>();

        // 依次扫描所有包
        for (String pkg : scanPackages) {
            logger.atDebug().log("scan package: {}", pkg);
            // 扫描一个包
            var rr = new ResourceResolver(pkg);
            List<Class<?>> classList = rr.scan();
            classNameSet.addAll(classList.stream().map(Class::getName).toList());
        }

        // 继续查找 @Import(Xyz.class) 导入的Class配置
        Import importConfig = configClass.getAnnotation(Import.class);
        if (importConfig != null) {
            for (Class<?> importConfigClass : importConfig.value()) {
                String importClassName = importConfigClass.getName();
                classNameSet.add(importClassName);
            }
        }
        return classNameSet;
    }

    // 根据 Name 查找 BeanDefinition，如果 Name 不存在，返回 null
    @Nullable
    public BeanDefinition findBeanDefinition(String name) {
        return this.beans.get(name);
    }

    // 根据Type查找若干个BeanDefinition，返回0个或多个:
    List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return this.beans.values().stream()
                .filter(def -> type.isAssignableFrom(def.getBeanClass()))
                .sorted().collect(Collectors.toList());
    }


    // 根据Type查找某个BeanDefinition，如果不存在返回null，如果存在多个返回 @Primary 标注的一个
    public BeanDefinition findBeanDefinition(Class<?> type) {
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.get(0);
        }
        // 多于一个时，查找 @Primary
        List<BeanDefinition> primaryDefs = defs.stream().filter(def -> def.isPrimary()).collect(Collectors.toList());
        if (primaryDefs.size() == 1) {
            return primaryDefs.get(0);
        }

        if (primaryDefs.isEmpty()) {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }
    }
}

