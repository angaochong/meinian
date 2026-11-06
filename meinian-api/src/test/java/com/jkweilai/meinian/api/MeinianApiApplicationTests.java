package com.jkweilai.meinian.api;

import com.jkweilai.meinian.api.db.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class MeinianApiApplicationTests {

    @Resource
    SysUserMapper sysUserMapper;

    @Test
    void contextLoads() {
        System.out.println(sysUserMapper.getPermissionsByUserId(1));
    }



}
