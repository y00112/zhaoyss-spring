package com.zhaoyss.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ModelAndView {

    private String view;

    private Map<String, Object> model;

    int status;

    public ModelAndView(String viewName) {
        this(viewName, HttpServletResponse.SC_OK, null);
    }

    public ModelAndView(String viewName, int status, @Nullable Map<String, Object> model) {
        this.view = viewName;
        this.status = status;
        if (model != null) {
            addModel(model);
        }
    }

    public <K, V> ModelAndView(String viewName, Map<String,Object> model) {
        this(viewName,HttpServletResponse.SC_OK,model);
    }

    private void addModel(Map<String, Object> map) {
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        this.model.putAll(map);
    }

    public int getStatus() {
        return this.status;
    }

    public String getViewName() {
        return this.view;
    }

    public Map<String, Object> getModel() {
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        return this.model;
    }
}
