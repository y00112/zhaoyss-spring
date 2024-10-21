package com.zhaoyss.web.web;

import com.zhaoyss.annotation.*;
import com.zhaoyss.exception.DataAccessException;
import com.zhaoyss.web.ModelAndView;
import com.zhaoyss.web.User;
import com.zhaoyss.web.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
public class MvcController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    static final String USERS_SESSION_KEY = "__user__";

    @GetMapping("/")
    ModelAndView index(HttpSession session){
        User user = (User) session.getAttribute(USERS_SESSION_KEY);
        if (user == null){
            return new ModelAndView("redirect:/register");
        }
        return new ModelAndView("/index.html", Map.of("user",user));
    }


    @GetMapping("/register")
    ModelAndView register(){
        return new ModelAndView("/register.html");
    }

    @PostMapping("/register")
    ModelAndView doRegister(@RequestParam("email")String email, @RequestParam("name") String name, @RequestParam("password") String password){
        try {
            userService.createUser(email,name,password);
        }catch (DataAccessException e){
            return new ModelAndView("/register.html",Map.of("error","Email already exist."));
        }
        return new ModelAndView("redirect:/signin");
    }

    @GetMapping("/signin")
    ModelAndView signing(){
        return new ModelAndView("/signin.html");
    }

    @PostMapping("/signin")
    ModelAndView doSignin(@RequestParam("email") String email, @RequestParam("password") String password,HttpSession session){
        User user;
        try {
            user = userService.getUser(email.strip().toLowerCase());
            if (!user.password.equals(password)){
                throw new DataAccessException("bad password.");
            }
        }catch (DataAccessException e){
            return new ModelAndView("/signin.html",Map.of("error","Bad email or password"));
        }
        session.setAttribute(USERS_SESSION_KEY,user);
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/signout")
    String signout(HttpSession session){
        session.removeAttribute(USERS_SESSION_KEY);
        return "redirect:/signin";
    }
}
