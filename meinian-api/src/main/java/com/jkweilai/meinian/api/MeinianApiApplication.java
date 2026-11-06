package com.jkweilai.meinian.api;

import com.jkweilai.meinian.api.async.InitializeWorkAsync;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

//开启异步线程
@EnableAsync
//让 Spring Boot 能够扫描和注册 Servlet 相关的组件。没有它，Servlet 组件不会自动注册。重新定义，走我们的
@ServletComponentScan
@ComponentScan("com.jkweilai.*")
@MapperScan(basePackages = "com.jkweilai.meinian.api.db.mapper")
@SpringBootApplication
@EnableCaching
//启用定时器（关闭订单）
@EnableScheduling
public class MeinianApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeinianApiApplication.class, args);
    }

    @Resource
    private InitializeWorkAsync initializeWorkAsync;

    // MeinianApiApplication 这个 bean 注入到容器后执行 init() 方法
    @PostConstruct
    public void init() {
        initializeWorkAsync.initializeWork();
    }

    // 通过重写run方法也可以。可以达到在服务器启动阶段就执行异步任务。
    /*
    @Override
    public void run(String... args) {
        initializeWorkAsync.initializeWork();
    }
    */

}
