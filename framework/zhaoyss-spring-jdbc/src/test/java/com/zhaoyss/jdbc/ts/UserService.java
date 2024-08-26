package com.zhaoyss.jdbc.ts;

import com.zhaoyss.annotation.Autowired;
import com.zhaoyss.annotation.Component;
import com.zhaoyss.annotation.Transactional;
import com.zhaoyss.jdbc.JdbcTemplate;
import com.zhaoyss.jdbc.JdbcTestBase;

@Component
@Transactional
public class UserService {

    @Autowired
    AddressService addressService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public User createUser(String name, int age) {
        Number id = jdbcTemplate.updateAndReturnGeneratedKey(JdbcTestBase.INSERT_USER, name, age);
        User user = new User();
        user.id = id.intValue();
        user.name = name;
        user.theAge = age;
        return user;
    }

    public User getUser(int userId){
        return jdbcTemplate.queryForObject(JdbcTestBase.SELECT_USER,User.class,userId);
    }

    public void updaterUser(User user){
        jdbcTemplate.update(JdbcTestBase.UPDATE_USER,user.name,user.theAge,user.id);
    }

    public void deleteUser(User user) {
        jdbcTemplate.update(JdbcTestBase.DELETE_USER, user.id);
        addressService.deleteAddress(user.id);
    }

}
