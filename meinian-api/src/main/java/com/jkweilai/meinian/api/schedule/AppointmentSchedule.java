package com.jkweilai.meinian.api.schedule;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.db.mapper.MedAppointmentLimitMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
/**
 * 预约日程管理类
 * 负责处理体检预约相关的缓存生成和调度任务
 */
public class AppointmentSchedule {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MedAppointmentLimitMapper medAppointmentLimitMapper;

    /**
     * 创建62天后的体检预约缓存
     * 每天23:00执行，预生成62天后的预约名额缓存
     *
     * 执行逻辑：
     * 1. 从Redis获取系统设置的每日预约名额数量
     * 2. 生成62天后的日期作为缓存key
     * 3. 在Redis中创建hash结构存储预约名额信息
     * 4. 设置缓存过期时间为生成日期的次日
     */
    @Scheduled(cron = "0 50 23 * * ?")  // 每天23:00执行
    public void createCacheAfter62Day() {

        // 从Redis系统设置中获取每日预约名额限制
        int actualLimit = Integer.parseInt(redisTemplate.opsForValue().get("setting#appointment_number").toString());

        // 初始化实际预约数量为0
        int actualCount = 0;

        // 计算62天后的日期，格式化为字符串
        String date = new DateTime().offset(DateField.DAY_OF_MONTH, 62).toDateStr();

        // 根据这个 date 查询限流规则。
        Map<String, Object> map = medAppointmentLimitMapper.selectByAppointmentDate(date);
        if(map != null){
            actualLimit = MapUtil.getInt(map, "actualLimit");
            actualCount = MapUtil.getInt(map, "actualCount");
        }

        // 构建Redis缓存key，格式：appointment#2024-01-01
        String key = "appointment#" + date;

        // 在Redis中创建hash结构，存储预约名额信息
        Map<String, Object> appointmentMap = new HashMap<>();
        appointmentMap.put("actualLimit", actualLimit);
        appointmentMap.put("actualCount", actualCount);

        redisTemplate.opsForHash().putAll(key, appointmentMap);

        // 设置缓存过期时间为生成日期的第二天（即63天后）
        DateTime dateTime = new DateTime(date).offsetNew(DateField.DAY_OF_MONTH, 1);
        redisTemplate.expireAt(key, dateTime);

        // 记录日志，便于监控和调试
        log.debug("生成了" + date + "的体检日程缓存");
    }
}