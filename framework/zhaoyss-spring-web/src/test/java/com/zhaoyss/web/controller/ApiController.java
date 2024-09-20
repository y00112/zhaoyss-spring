package com.zhaoyss.web.controller;

import com.zhaoyss.annotation.GetMapping;
import com.zhaoyss.annotation.PathVariable;
import com.zhaoyss.annotation.ResponseBody;
import com.zhaoyss.annotation.RestController;
import com.zhaoyss.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
public class ApiController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/api/hello/{name}")
    @ResponseBody
    String hello(@PathVariable("name") String name) {
        return JsonUtils.writeJson(Map.of("name", name));
    }
}
