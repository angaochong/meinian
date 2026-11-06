package com.jkweilai.meinian.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration  // 声明这是一个配置类
public class RedisTemplateConfig {

    @Bean  // 将返回的对象注册为Spring Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 设置普通 key 的序列化方式为字符串
        template.setKeySerializer(new StringRedisSerializer());
        // 设置普通 value 的序列化方式为字符串
        template.setValueSerializer(new StringRedisSerializer());

        // 设置 Hash 数据结构的 key 序列化方式为字符串
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置 Hash 数据结构的 value 序列化方式为 JSON
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置 Redis 连接工厂
        template.setConnectionFactory(factory);
        return template;
    }
}
