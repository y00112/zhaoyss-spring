package com.zhaoyss.web;

import com.zhaoyss.annotation.Controller;
import com.zhaoyss.annotation.GetMapping;
import com.zhaoyss.annotation.PostMapping;
import com.zhaoyss.annotation.RestController;
import com.zhaoyss.content.ApplicationContext;
import com.zhaoyss.content.ConfigurableApplicationContext;
import com.zhaoyss.entity.A;
import com.zhaoyss.exception.ServerErrorException;
import com.zhaoyss.exception.ServerWebInputException;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.web.utils.JsonUtils;
import com.zhaoyss.web.utils.WebUtils;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    List<Dispatcher> getDispatchers = new ArrayList<>();

    List<Dispatcher> postDispatchers = new ArrayList<>();

    final Logger logger = LoggerFactory.getLogger(getClass());

    ApplicationContext applicationContext;

    public DispatcherServlet(ApplicationContext applicationContext, PropertyResolver propertyResolver){
        this.applicationContext = applicationContext;
    }

    private void doService(HttpServletRequest req,HttpServletResponse resp, List<Dispatcher> dispatchers){
        String url = req.getRequestURI();
        try {
            doService(url,req,resp,dispatchers);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void doService(String url, HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers) throws Exception{
        for (Dispatcher dispatcher : dispatchers){
            Dispatcher.Result result = dispatcher.process(url, req, resp);
            if (result.processed()){

            }
        }
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

    private void addController(boolean isRest, String name, Object instance) throws ServletException {
        logger.info("add {} controller '{}' : {}",isRest ? "REST" : "MVC",name,instance.getClass().getName());
        addMethods(isRest,name,instance,instance.getClass());
    }

    private void addMethods(boolean isRest, String name,Object instance, Class<?> type) throws ServletException {
        for (Method m : type.getDeclaredMethods()){
            GetMapping get = m.getAnnotation(GetMapping.class);
            if (get != null){
                checkMethod(m);
                this.getDispatchers.add(new Dispatcher("GET",isRest,instance,m,get.value()));
            }
            PostMapping post = m.getAnnotation(PostMapping.class);
            if (post != null){
                checkMethod(m);
                this.postDispatchers.add(new Dispatcher("POST",isRest,instance,m,post.value()));
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null){
            addMethods(isRest,name,instance,superClass);
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
    private static final Result NOT_PROCESSED = new Result(false,null);
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

    public Dispatcher(String httpMethod, boolean isRest, Object controller, Method method, String urlPattern) {

    }
    Result process(String url, HttpServletRequest request, HttpServletResponse response) throws Exception{
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.matches()){
            Object[] arguments = new Object[this.methodParameters.length];
            for (int i = 0; i < arguments.length; i++) {
                Param param = methodParameters[i];
                arguments[i] = switch (param.paramType){
                    case PATH_VARIABLE -> {
                        try {
                            String s = matcher.group(param.name);
                            yield convertToType(param.classType,s);
                        }catch (IllegalArgumentException e){
                            throw new ServerWebInputException("Path variable '" + param.name + "' not found.");
                        }
                    }
                    case REQUEST_BODY -> {
                        BufferedReader reader =request.getReader();
                        yield JsonUtils.readJson(reader,param.classType);
                    }
                    case REQUEST_PARAM -> {
                        String s = getOrDefault(request,param.name,param.defaultValue);
                        yield convertToType(param.classType,s);
                    }
                    case SERVLET_VARIABLE -> {
                        Class<?> classType = param.classType;
                        if (classType == HttpServletRequest.class){
                            yield request;
                        }else if (classType == HttpServletResponse.class){
                            yield response;
                        }else if (classType == HttpSession.class){
                            yield request.getSession();
                        }else if (classType == ServletContext.class){
                            yield request.getServletContext();
                        }else {
                            throw new ServerErrorException("Could not determine argument type: "  + classType);
                        }
                    }
                };
            }
            Object result = null;
            try {
                result = this.handlerMethod.invoke(this.controller,arguments);
            }catch (InvocationTargetException e){
                Throwable t = e.getCause();
                if (t instanceof Exception ex){
                    throw ex;
                }
                throw e;
            }catch (ReflectiveOperationException e){
                throw new ServerErrorException(e);
            }
            return new Result(true,result);
        }
        return NOT_PROCESSED;
    }

    static record Result(boolean processed,Object returnObject){}

    private String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String s = request.getParameter(name);
        if (s == null){
            if (WebUtils.DEFAULT_PARAM_VALUE.equals(defaultValue)){
                throw new ServerWebInputException("Request parameter '" + name + "' not found.");
            }
            return defaultValue;
        }
        return s;
    }

    private Object convertToType(Class<?> classType, String s) {
        if (classType == String.class){
            return s;
        } else if (classType == boolean.class || classType == Boolean.class) {
            return Boolean.valueOf(s);
        }else if (classType == int.class || classType == Integer.class) {
            return Integer.valueOf(s);
        } else if (classType == long.class || classType == Long.class) {
            return Long.valueOf(s);
        } else if (classType == byte.class || classType == Byte.class) {
            return Byte.valueOf(s);
        } else if (classType == short.class || classType == Short.class) {
            return Short.valueOf(s);
        } else if (classType == float.class || classType == Float.class) {
            return Float.valueOf(s);
        } else if (classType == double.class || classType == Double.class) {
            return Double.valueOf(s);
        } else {
            throw new ServerErrorException("Could not determine argument type: " + classType);
        }
    }

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
