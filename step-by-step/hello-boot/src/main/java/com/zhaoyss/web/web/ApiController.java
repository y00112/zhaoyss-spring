package com.zhaoyss.web.web;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.GetMapping;
import com.zhaoyss.annotation.PathVariable;
import com.zhaoyss.annotation.RestController;
import com.zhaoyss.exception.DataAccessException;
import com.zhaoyss.web.User;
import com.zhaoyss.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @GetMapping("/api/user/{email}")
    Map<String,Boolean> userExist(@PathVariable("email") String email){
        if (email.contains("@")){
            throw new IllegalArgumentException("Invalid email");
        }
        try{
            userService.getUser(email);
            return Map.of("result",Boolean.TRUE);
        }catch (DataAccessException e){
            return Map.of("result",Boolean.FALSE);
        }
    }

    @GetMapping("/api/users")
    List<User> users(){
        return userService.getUsers();
    }
}
