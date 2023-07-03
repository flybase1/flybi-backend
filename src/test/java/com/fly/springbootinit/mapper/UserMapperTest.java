package com.fly.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {
    @Resource
    private UserMapper userMapper;

    @Test
    void testUpdateTo100() {
        boolean b = userMapper.updateAllUserLeftCountTo100();
        if (!b){
            System.out.println("error");
        }
        System.out.println("ok");
    }
}