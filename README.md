# zhaoyss-spring
为了进一步理解Spring的设计思想，提升自己的架构能力，决定参考[廖雪峰老师的手写Spring教程](https://liaoxuefeng.com/books/summerframework/introduction/index.html)，实现Spring-Framework的核心功能。
设计目标：
- ioc模块：实现ApplicationContext容器与Bean的管理
- app模块：实现 AOP 功能
- jdbc模块：实现JdbcTemplate，以及声明式实物管理
- web模块：实现Web MVC和 REST API
- boot模块：实现一个简化版的 “Spring Boot”，用于打包运行

# 开发进度
## content：
- 实现ResourceResolver，扫描指定包下的 .class 文件
- 实现PropertyResolver，加载并存储配置文件为 K-V 形式，并获取。
- 创建BeanDefinition，并设置 BeanDefinition 的属性。
- 创建Bean实例，并通过构造方法和工厂方法注入Bean。
- 通过字段和set方法注入bean，并调用init方法。
- 实现BeanPostProcessor，允许用户自定义Bean替换原始Bean
- 完成ioc容器
## aop:
- 通过 ByteBuddy + Annotation 方式实现动态代理
- @Around 注解 AroundProxyBeanPostProcessor 实现 AOP
  - 测试用例: [framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/around/AroundProxyTest.java](framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/around/AroundProxyTest.java)
- @Before
  - 测试用例: [framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/before/BeforeProxyTest.java](framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/before/BeforeProxyTest.java)
- @After
  - 测试用例：[framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/after/AfterProxyTest.java](framework/zhaoyss-spring-aop/src/test/java/com/zhaoyss/after/AfterProxyTest.java)
## jdbc:
- 实现JdbcTemplate
- 实现声明式事务
