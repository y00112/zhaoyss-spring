package com.zhaoyss;

import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.io.PropertyResolver;
import com.zhaoyss.web.DispatcherServlet;
import com.zhaoyss.web.WebMvcConfiguration;
import com.zhaoyss.web.controller.ControllerConfiguration;
import com.zhaoyss.web.utils.JsonUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DispatcherServletTest {

    DispatcherServlet dispatcherServlet;
    MockServletContext ctx;

    @BeforeEach
    void init() throws ServletException {
        this.ctx = createMockServletContext();
        WebMvcConfiguration.setServletContext(this.ctx);
        var propertyResolver = createPropertyResolver();
        var applicationContext = new AnnotationConfigApplicationContext(ControllerConfiguration.class, propertyResolver);
        this.dispatcherServlet = new DispatcherServlet(applicationContext, propertyResolver);
        this.dispatcherServlet.init();
        ;
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("zhaoyss.web.favicon-path", "/icon/favicon.ico");
        ps.put("zhaoyss.web.freemarker.template-path", "/WEB-INF/templates");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        var pr = new PropertyResolver(ps);
        return pr;
    }

    @Test
    void getHello() throws ServletException, IOException {
        var req = createMockRequest("GET", "/hello/Bob", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("Hello,Bob", resp.getContentAsString());
    }

    @Test
    void getApiHello() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/hello/Bob", null, null);
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType());
        assertEquals("{\"name\":\"Bob\"}", resp.getContentAsString());
    }

    @Test
    void getGreeting() throws IOException, ServletException {
        var req = createMockRequest("GET", "/greeting", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req, resp);
        assertEquals(200, resp.getStatus());
        assertEquals("Hello;Bob", resp.getContentAsString());
    }

    @Test
    void getApiGreeting() throws ServletException, IOException {
        var req = createMockRequest("GET", "/api/greeting", null, Map.of("name", "Bob"));
        var resp = createMockResponse();
        this.dispatcherServlet.service(req,resp);
        assertEquals(200,resp.getStatus());
        assertEquals("application/json",resp.getContentType());
        assertEquals("{\"action\":{\"name\":\"Bob\"}}", resp.getContentAsString());
    }

    MockServletContext createMockServletContext() {
        Path path = Path.of("./src/test/resource").toAbsolutePath().normalize();
        var ctx = new MockServletContext("file://" + path.toString());
        ctx.setRequestCharacterEncoding("UTF-8");
        ctx.setResponseCharacterEncoding("UTF-8");
        return ctx;
    }

    MockHttpServletRequest createMockRequest(String method, String path, Object body, Map<String, String> params) {
        var req = new MockHttpServletRequest(this.ctx, method, path);
        if (method.equals("GET") && params != null) {
            params.keySet().forEach(key -> {
                req.setParameter(key, params.get(key));
            });
        } else if (method.equals("POST")) {
            if (body != null) {
                req.setContentType("application/json");
                req.setContent(JsonUtils.writeJson(body).getBytes(StandardCharsets.UTF_8));
            } else {
                req.setContentType("application/x-www-form-urlencoded");
                if (params != null) {
                    params.keySet().forEach(key -> {
                        req.setParameter(key, params.get(key));
                    });
                }
            }
        }
        var session = new MockHttpSession();
        req.setSession(session);
        return req;
    }

    MockHttpServletResponse createMockResponse() {
        var resp = new MockHttpServletResponse();
        resp.setDefaultCharacterEncoding("UTF-8");
        return resp;
    }
}
