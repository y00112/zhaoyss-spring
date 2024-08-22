package com.zhaoyss.content;

import com.zhaoyss.annotation.*;
import com.zhaoyss.annotation.Component;
import com.zhaoyss.exception.*;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.io.ResourceResolver;
import com.zhaoyss.utils.ClassUtils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保存所有的 bean
 */
public class AnnotationConfigApplicationContext implements ConfigurableApplicationContext {

    final Logger logger = LoggerFactory.getLogger(getClass());

    final PropertyResolver propertyResolver;

    Map<String, BeanDefinition> beans;

    // 在创建 Bean实例的时候，跟踪当前正在创建的所有Bean的名称。检查循环依赖问题。
    Set<String> creatingBeanNames;

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();


    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) {
        ApplicationContextUtils.setApplicationContext(this);
        this.propertyResolver = propertyResolver;
        // 扫描获取所有Bean的Class类型
        Set<String> beanClassNames = scanForClassNames(configClass);
        // 创建 Bean 的定义
        this.beans = createBeanDefinitions(beanClassNames);
        // 创建BeanName检测循环依赖
        this.creatingBeanNames = new HashSet<>();
        // 创建 @Configuration 类型的 Bean
        this.beans.values().stream()
                // 过滤出@Configuration
                .filter(this::isConfigurationDefinition).sorted().map(def -> {
                    // 创建Bean实例
                    createBeanAsEarlySingleton(def);
                    return def.getName();
                }).collect(Collectors.toList());
        // 创建 BeanPostProcessor 类型的Bean
        List<BeanPostProcessor> processors = this.beans.values().stream()
                // 过滤出 BeanPostProcessor
                .filter(this::isBeanPostProcessorDefinition)
                .sorted()
                // 创建BeanPostProcessor实例
                .map(def->{
                    return (BeanPostProcessor) createBeanAsEarlySingleton(def);
                }).collect(Collectors.toList());

        this.beanPostProcessors.addAll(processors);

        // 创建其他普通Bean:
        List<BeanDefinition> defs = this.beans.values().stream()
                // 过滤出 instance == null 的BeanDefinition
                .filter(def -> def.getInstance() == null)
                .collect(Collectors.toList());

        // 循环创建bean的实例
        for (BeanDefinition def : defs) {
            // 如果Bean未被创建（可能在其他Bean的构造方法注入前被创建）
            if (def.getInstance() == null) {
                // 创建bean
                createBeanAsEarlySingleton(def);
            }
        }

        // 通过字段和 set 方法注入依赖
        this.beans.values().forEach(def->{
            injectBean(def);
        });

        // 调用 init 方法
        this.beans.values().forEach(def->{
            initBean(def);
        });

        if (logger.isDebugEnabled()) {
            this.beans.values().stream().sorted().forEach(def -> {
                logger.debug("bean initialized: {}", def);
            });
        }
    }

    // 注入依赖单不调用 init 方法
    private void injectBean(BeanDefinition def) {
        // 获取Bean实例,或者被代理的原始实例
        Object beanInstance = getProxiedInstance(def);
        try {
            injectProperties(def,def.getBeanClass(),beanInstance);
        }catch (ReflectiveOperationException e){
            throw new BeanCreationException(e);
        }
    }

    private Object getProxiedInstance(BeanDefinition def) {
        Object beanInstance = def.getInstance();
        // 如果Proxy改变了原始Bean，又希望注入到原始Bean，则由BeanPostProcessor指定原始Bean
        List<BeanPostProcessor> reversedBeanPostProcessors = new ArrayList<>(this.beanPostProcessors);
        Collections.reverse(reversedBeanPostProcessors);
        for(BeanPostProcessor beanPostProcessor: reversedBeanPostProcessors){
            Object restoredInstance = beanPostProcessor.postProcessOnSetProperty(beanInstance, def.getName());
            if (restoredInstance != beanInstance){
                beanInstance = restoredInstance;
            }
        }
        return beanInstance;
    }

    // 在当前类以及父类进行字段和方法注入
    private void injectProperties(BeanDefinition def, Class<?> clazz, Object bean) throws ReflectiveOperationException {
        // 在当前类查找 Field 和 Method 并注入
        for (Field f :clazz.getDeclaredFields()){
            tryInjectProperties(def,clazz,bean,f);
        }
        for (Method m : clazz.getDeclaredMethods()){
            tryInjectProperties(def,clazz,bean,m);
        }
    }

    // 尝试注入单个属性
    private void tryInjectProperties(BeanDefinition def, Class<?> clazz, Object bean, AccessibleObject acc) throws ReflectiveOperationException {
        Value value = acc.getAnnotation(Value.class);
        Autowired autowired = acc.getAnnotation(Autowired.class);
        if (value == null && autowired == null){
            return;
        }

        Field field = null;
        Method method = null;
        if (acc instanceof Field f){
            checkFieldOrMethod(f);
            f.setAccessible(true);
            field = f;
        }
        if (acc instanceof Method m){
            checkFieldOrMethod(m);
            if (m.getParameters().length != 1){
                throw new BeanDefinitionException(
                        String.format("Cannot inject a non-setter method %s for bean '%s': %s", m.getName(), def.getName(), def.getBeanClass().getName()));
            }
            m.setAccessible(true);
            method = m;
        }
        String accessibleName = field != null ? field.getName():method.getName();
        Class<?> accessibleType = field != null? field.getType() : method.getParameterTypes()[0];

        if (value != null && autowired != null){
            throw new BeanCreationException(String.format("Cannot specify both @Autowired and @Value when inject %ss.%s for bean '%s':'%s'",
                    clazz.getSimpleName(),accessibleName,def.getName(),def.getBeanClass().getName()));
        }

        // @Value 注入
        if (value != null){
            Object propValue = this.propertyResolver.getRequiredProperty(value.value(),accessibleType);
            if (field != null){
                logger.atDebug().log("Field injection: {}.{} = {}",def.getBeanClass().getName(),accessibleName,propValue);
                field.set(bean,propValue);
            }
            if (method != null){
                logger.atDebug().log("Method injection: {}.{} ({})",def.getBeanClass().getName(),accessibleName,propValue);
                method.invoke(bean,propValue);
            }
        }

        // @Autowired 注入
        if (autowired != null){
            String name = autowired.name();
            boolean required = autowired.value();
            Object depends = name.isEmpty() ? findBean(accessibleType) : findBean(name,accessibleType);
            if (required && depends == null){
                throw new UnsatisfiedDependencyException(String.format("Dependency bean not found when inject %s.%s for bean ‘%s':%s",clazz.getSimpleName(),accessibleName,def.getName(),def.getBeanClass().getName()));
            }
            if (depends != null){
                if (field != null){
                    logger.atDebug().log("Field injection: {}.{} = {}",def.getBeanClass().getName(),accessibleName,depends);
                    field.set(bean,depends);
                }
                if (method != null){
                    logger.atDebug().log("Mield injection: {}.{} ({})",def.getBeanClass().getName(),accessibleName,depends);
                    method.invoke(bean,depends);
                }

            }
        }
    }

    private <T> T findBean(String name, Class<T> accessibleType) {
        BeanDefinition def = findBeanDefinition(name, accessibleType);
        if (def == null){
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T findBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null){
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    private void checkFieldOrMethod(Member m) {
        int mode = m.getModifiers();
        if (Modifier.isStatic(mode)){
            throw new BeanDefinitionException("Cannot inject static field: " + m);
        }
        if (Modifier.isFinal(mode)){
            if (m instanceof Field field){
                throw new BeanDefinitionException("Cannot inject final field: " + field);
            }
            if (m instanceof Method method){
                logger.warn("Inject final method should be careful because it is not called on target bean when bean is proxied and may cause NullPointerException.");
            }
        }
    }

    private void initBean(BeanDefinition def) {
        // 调用 init 方法
        callMethod(def.getInstance(),def.getInitMethod(),def.getInitMethodName());
    }

    private void callMethod(Object beanInstance, Method method, String nameMethod) {
        // 调用 init/destroy 方法
        if (method != null){
            try {
                method.invoke(beanInstance);
            } catch (ReflectiveOperationException e) {
                throw new BeanCreationException(e);
            }
        }else if (nameMethod != null){
            // 查找 initMethod/destroyMethod = "xyz",注意是在实际类型种查找
            Method named = ClassUtils.getNamedMethod(beanInstance.getClass(),nameMethod);
            named.setAccessible(true);
            try {
                named.invoke(beanInstance);
            } catch (ReflectiveOperationException e) {
                throw new BeanCreationException(e);
            }
        }
    }

    /**
     * 创建一个 Bean，但不进行字段和方法级别的注入。如果创建的Bean不是Configuration，则在构造方法/工厂方法中注入的依赖Bean会自动创建
     */
    public Object createBeanAsEarlySingleton(BeanDefinition def) {
        boolean bool = this.creatingBeanNames.add(def.name);
        if (!bool) {
            // 检测到重复创建Bean导致循环依赖
            throw new UnsatisfiedDependencyException(String.format("Circular dependency detected when create bean '%s'", def.getName()));
        }

        // 创建方法：构造方法和工厂方法
        Executable crateFn = def.getFactoryName() == null ?
                def.getConstructor() : def.getFactoryMethod();

        // 创建参数：
        Parameter[] parameters = crateFn.getParameters();
        Annotation[][] parametersAnnos = crateFn.getParameterAnnotations();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Annotation[] parametersAnno = parametersAnnos[i];
            // 从参数获取 @Value 和 @Autowired
            Value value = ClassUtils.getAnnotation(parametersAnno, Value.class);
            Autowired autowired = ClassUtils.getAnnotation(parametersAnno, Autowired.class);

            // @Configuration类型的Bean是工厂，不允许使用@Autowired创建
            final boolean isConfiguration = isConfigurationDefinition(def);
            if (isConfiguration && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when create @Configuration bean '%s': %s.", def.getName(), def.getBeanClass().getName())
                );
            }

            //BeanPostProcessor 不能依赖其他 Bean， 不允许使用 @Autowired创建
            final boolean isBeanPostProcessor = isBeanPostProcessorDefinition(def);
            if (isBeanPostProcessor && autowired != null){
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when creat BeanPostProcessor ’%s‘:%s.",def.getName(),def.getBeanClass().getName())
                );
            }
            // 检查 Value 和 Autowired
            if (value != null && autowired != null) {
                throw new BeanCreationException(
                        String.format("在创建 Bean '%s': %s 时，无法同时指定 @Autowired 和 @Value.", def.getName(), def.getBeanClass().getName())
                );
            }
            if (value == null && autowired == null) {
                throw new BeanCreationException(
                        String.format("必须指定 @Autowired or @Value 在创建 Bean '%s': %s.", def.getName(), def.getBeanClass().getName())
                );
            }

            // 参数类型
            Class<?> type = param.getType();
            if (value != null) {
                // 参数设置为查询到 @Value
                args[i] = this.propertyResolver.getRequiredProperty(value.value(), type);
            } else {
                // 参数是@Autowired.
                String name = autowired.name();
                boolean required = autowired.value();
                // 依赖的BeanDefinition：
                BeanDefinition dependsOnDef = name.isEmpty() ? findBeanDefinition(type) : findBeanDefinition(name, type);
                // 检测required==true?
                if (required && dependsOnDef == null) {
                    throw new BeanCreationException(String.format("Missing autowired bean with type '%s' when create bean '%s': %s.", type.getName(),
                            def.getName(), def.getBeanClass().getName()));
                }
                if (dependsOnDef != null) {
                    // 获取依赖Bean:
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    if (autowiredBeanInstance == null && !isConfiguration && !isBeanPostProcessor) {
                        // 当前依赖Bean尚未初始化，递归调用初始化该依赖Bean:
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                } else {
                    args[i] = null;
                }
            }
        }

        // 已拿到所有方法参数，创建Bean实例
        Object instance = null;
        if (def.getFactoryName() == null) {
            // 用构造方法创建
            try {
                instance = def.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        } else {
            // 用@Bean 方法创建
            Object configInstance = getBean(def.getFactoryName());
            try {
                instance = def.getFactoryMethod().invoke(configInstance, args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        }
        def.setInstance(instance);

        // 调用BeanPostProcessor处理Bean
        for (BeanPostProcessor processor: beanPostProcessors){
            Object processed = processor.postProcessBeforeInitialization(def.getInstance(), def.getName());
            if (processed == null){
                throw new BeanCreationException(String.format("PostBeanProcessor returns null when process bean '%s' by %s.",def.getName(),processor));
            }
            // 如果一个 BeanPostProcess 替换了原始Bean，则更新Bean的引用
            if (def.getInstance() != processed){
                logger.atDebug().log("Bean {} was replaced by post processor {}.",def.getName(),processor.getClass().getName());
                def.setInstance(processed);
            }
        }
        return def.getInstance();
    }

    @Override
    public boolean containsBean(String name) {
        return this.beans.containsKey(name);
    }

    /**
     * 通过Name查找Bean，不存在时抛出 NoSuchBeanDefinitionException
     */
    @SuppressWarnings("unchecked")
    public  <T> T getBean(String name) {
        BeanDefinition def = this.beans.get(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with name '%s'.", name));
        }
        return (T) def.getRequiredInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with type '%s'.", requiredType));
        }
        return (T) def.getRequiredInstance();
    }

    @Override
    public <T> List<T> getBeans(Class<?> requiredType) {
        List<BeanDefinition> defs = findBeanDefinitions(requiredType);
        if (defs.isEmpty()){
            return List.of();
        }
        List<T> list = new ArrayList<>(defs.size());
        for (var def: defs){
            list.add((T) def.getRequiredInstance());
        }
        return list;
    }

    @Override
    public void close() {
        logger.info("Closing {}...",this.getClass().getName());
        this.beans.values().forEach(def ->{
            final Object beanInstance = getProxiedInstance(def);
            callMethod(beanInstance,def.getDestroyMethod(),def.getDestroyMethodName());
        });
        this.beans.clear();
        logger.info("{} closed.",this.getClass().getName());
        ApplicationContextUtils.setApplicationContext(null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name,Class<T> requiredType){
        T t = findBean(name,requiredType);
        if (t == null){
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with name '%s' and type '%s'.", name, requiredType));
        }
        return t;
    }

    private boolean isConfigurationDefinition(BeanDefinition def) {
        return ClassUtils.findAnnotation(def.getBeanClass(), Configuration.class) != null;
    }

    private boolean isBeanPostProcessorDefinition(BeanDefinition def){
        return BeanPostProcessor.class.isAssignableFrom(def.getBeanClass());
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
                logger.atDebug().log("found component: {}", clazz.getName());
                // 获取 Bean 的名称
                String beanName = ClassUtils.getBeanName(clazz);
                var def = new BeanDefinition(beanName, clazz, getSuitableConstructor(clazz), getOrder(clazz), clazz.isAnnotationPresent(Primary.class),
                        null, null,
                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
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
     *
     * @Configuration public class AppConfiguration{
     * @Bean ZoneId createZone(){
     * return ZoneId.of("Z");
     * }
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

                var def = new BeanDefinition(ClassUtils.getBeanName(method), beanClass, factoryBeanName, method, getOrder(method),
                        method.isAnnotationPresent(Primary.class),
                        bean.initMethod().isEmpty() ? null : bean.initMethod(),
                        bean.destroyMethod().isEmpty() ? null : bean.destroyMethod(),
                        null, null);
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

    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            return null;
        }
        if (!requiredType.isAssignableFrom(def.getBeanClass())) {
            throw new BeanNotOfRequiredTypeException(String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.", requiredType.getName(),
                    name, def.getBeanClass().getName()));

        }
        return def;
    }


    // 根据Type查找若干个BeanDefinition，返回0个或多个:
    public List<BeanDefinition> findBeanDefinitions(Class<?> type) {
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

