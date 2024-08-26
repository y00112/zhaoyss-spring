package com.zhaoyss.jdbc.ts;

import com.sun.source.tree.AssertTree;
import com.zhaoyss.content.AnnotationConfigApplicationContext;
import com.zhaoyss.exception.TransactionException;
import com.zhaoyss.jdbc.JdbcTemplate;
import com.zhaoyss.jdbc.JdbcTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcTxTest {

    @Test
    public void testJdbcTx(){
        JdbcTestBase.beforeEach();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(JdbcTxApplication.class, JdbcTestBase.createPropertyResolver());
        JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
        jdbcTemplate.update(JdbcTestBase.CREATE_USER);
        jdbcTemplate.update(JdbcTestBase.CREATE_ADDRESS);

        UserService userService = ctx.getBean(UserService.class);
        AddressService addressService = ctx.getBean(AddressService.class);
        // 代理
        assertNotSame(UserService.class,userService.getClass());
        assertNotSame(AddressService.class,addressService.getClass());
        // 代理对象未注入
        assertNull(userService.addressService);
        assertNull(addressService.userService);
        // 增
        User bob = userService.createUser("bob", 22);
        System.out.println(bob);

        Address addr1 = new Address(bob.id, "Bei Jing",10010);
        Address addr2 = new Address(bob.id, "Shang Hai",10012);
        Address addr3 = new Address(bob.id + 1, "Ocean Drive, Miami, Florida", 33411);

        // 事务异常回滚
        Assertions.assertThrows(TransactionException.class,()->{
            addressService.addAddress(addr1,addr2,addr3);
        });

        // 判断是否正常回滚
        assertTrue(addressService.getAddresses(bob.id).isEmpty());

        // 插入 addr1 addr2 给 bob
        addressService.addAddress(addr1,addr2);
        assertEquals(2, addressService.getAddresses(bob.id).size());

        // 删除 bob 导致回滚
        assertThrows(TransactionException.class,()->{
            userService.deleteUser(bob);
        });

        // bob 和 address 仍然存在
        assertEquals("bob",userService.getUser(1).name);
        assertEquals(2,addressService.getAddresses(bob.id).size());


    }
}
