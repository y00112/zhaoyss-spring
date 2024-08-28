package com.zhaoyss.web;

import com.zhaoyss.annotation.Controller;
import com.zhaoyss.annotation.GetMapping;
import com.zhaoyss.annotation.RestController;
import com.zhaoyss.content.ApplicationContext;
import com.zhaoyss.content.ConfigurableApplicationContext;
import com.zhaoyss.entity.A;
import com.zhaoyss.io.PropertyResolver;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    List<Dispatcher> getDispatchers = new ArrayList<>();

    List<Dispatcher> postDispatchers = new ArrayList<>();

    final Logger logger = LoggerFactory.getLogger(getClass());

    ApplicationContext applicationContext;

    public DispatcherServlet(ApplicationContext applicationContext, PropertyResolver propertyResolver){
        this.applicationContext = applicationContext;
    }

    @Override
    public void init() throws ServletException {
        logger.info("init {}.",getClass().getName());
        // scan @Controller and @RestController
        for (var def: ((ConfigurableApplicationContext)this.applicationContext).findBeanDefinitions(Object.class)){
            Class<?> beanClass = def.getBeanClass();
            Object bean = def.getRequiredInstance();
            Controller controller = beanClass.getAnnotation(Controller.class);
            RestController restController = beanClass.getAnnotation(RestController.class);
            if (controller != null && restController != null){
                throw new ServletException("Found @Controller and @RestController on class:" + beanClass.getName());
            }
            if (controller != null){
                addController(false,def.getName(),bean);
            }
            if (restController != null){
                addController(true,def.getName(),bean);
            }
        }
    }

    private void addController(boolean isRest, String name, Object instance) {
        logger.info("add {} controller '{}' : {}",isRest ? "REST" : "MVC",name,instance.getClass().getName());
        addMethods(isRest,name,instance.getClass());
    }

    private void addMethods(boolean isRest, String name, Class<?> type) {
        for (Method m : type.getDeclaredMethods()){
            GetMapping get = m.getAnnotation(GetMapping.class);
            if (get != null){
                try {
                    checkMethod(m);
                } catch (ServletException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkMethod(Method m) throws ServletException {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)){
            throw new ServletException("Cannot do URL mapping to static method: "+ m);
        }
        m.setAccessible(true);
    }

    @Override
    public void destroy() {
        this.applicationContext.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        // 依次匹配每个 Dispatcher 的 URL
        for (Dispatcher dispatcher : getDispatchers){
        }
    }
}
class Dispatcher {
    // 是否返回REST
    boolean isRest;
    // 是否有 @ResponseBody;
    boolean isResponseBody;
    // 是否返回 void
    boolean isVoid;
    // URL 正则匹配
    Pattern urlPattern;
    // Bean实例
    Object controller;
    // 处理方法
    Method handlerMethod;
    // 方法参数
    Param[] methodParameters;
}

class Param{
    // 参数名称
    String name;
    // 参数类型
    ParamType paramType;
    // 参数Class类型
    Class<?> classType;
    // 参数默认值
    String defaultValue;

}
enum ParamType{
    PATH_VARIABLE,
    REQUEST_PARAM,
    REQUEST_BODY,
    SERVLET_VARIABLE

}
