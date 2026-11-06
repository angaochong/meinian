package com.jkweilai.meinian.api.async;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.db.mapper.MedAppointmentLimitMapper;
import com.jkweilai.meinian.api.db.mapper.MedDepartmentFlowMapper;
import com.jkweilai.meinian.api.db.mapper.SysConfigMapper;
import com.jkweilai.meinian.api.db.pojo.SysConfigEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 异步初始化
 */
@Slf4j
@Component
public class InitializeWorkAsync {

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MedAppointmentLimitMapper  medAppointmentLimitMapper;

    @Resource
    private MedDepartmentFlowMapper medDepartmentFlowMapper;

    // 让 initializeWork() 方法在一个名为 "AsyncTaskExecutor" 的线程池中异步执行
    // 这个线程池我们之前配置过，在ThreadPoolConfig中配置的。这里可以直接使用。
    @Async("AsyncTaskExecutor")
    public void initializeWork() {
        // 查询所有系统默认配置
        List<SysConfigEntity> sysConfigEntities = sysConfigMapper.selectAll();
        // 遍历系统配置，将其放到redis缓存中
        sysConfigEntities.forEach(sysConfig -> {
            String configKey = sysConfig.getConfigKey();
            String configValue = sysConfig.getConfigValue();
            redisTemplate.opsForValue().set("setting#" + configKey, configValue);
        });
        // 记一下日志
        log.debug("系统配置已加载到redis缓存");

        // 获取次日日期
        DateTime startDate = DateUtil.tomorrow();
        // 获取60天之后的日期
        DateTime endDate = startDate.offsetNew(DateField.DAY_OF_MONTH, 60);
        // 获取一个60天的日期集合
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        // 将开始日期和结束日期放到Map集合中，这个是SQL语句的查询条件
        Map<String, Object> param = new HashMap<>();
        param.put("startDate", startDate.toDateStr());
        param.put("endDate", endDate.toDateStr());
        // 查询数据库中所有的限流规则
        List<Map<String, Object>> list = medAppointmentLimitMapper.selectAppointmentLimitLast60Days(param);
        // 遍历日期集合
        range.forEach(date -> {
            // 默认的系统配置规则先取出来
            int actualLimit = Integer.parseInt(redisTemplate.opsForValue().get("setting#appointment_number").toString());
            int actualCount = 0;
            // 判断日期date在list集合中有没有对应的限流规则
            // 如果有，则将对应的限流规则取出放到redis缓存中
            // 如果没有，则需要根据系统配置创建一个全新的限流规则，存放到redis中

            // date是2025-11-04 21:26:41这样的，把它变成2025-11-04这样。
            String dateStr = date.toDateStr();
            for(Map<String, Object> map : list) {
                String appointmentDate = MapUtil.getStr(map, "appointmentDate");
                if(dateStr.equals(appointmentDate)) {
                    actualLimit = MapUtil.getInt(map, "actualLimit");
                    actualCount = MapUtil.getInt(map, "actualCount");
                    break;
                }
            }

            // 存储到redis缓存中
            Map<String, Object> cache = new HashMap<>();
            cache.put("actualLimit", actualLimit);
            cache.put("actualCount", actualCount);
            String key = "appointment#" + dateStr;
            // 注意：向redis中存储Map需要调用这个方法。
            redisTemplate.opsForHash().putAll(key, cache);
            // 设置缓存过期时间（每一条缓存的过期时间是当前日期+1）
            DateTime dateTime = new DateTime(dateStr).offsetNew(DateField.DAY_OF_MONTH, 1);
            redisTemplate.expireAt(key, dateTime);
        });

        // 记录日志
        log.debug("60天的限流规则已全部加载到redis缓存");

        medDepartmentFlowMapper.updateCurrentCount(Map.of("currentCount", 0));

        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation").toString();
        boolean mode = Boolean.parseBoolean(value);
        List<Map<String, Object>> recommendations = null;

        if(mode){
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithWeight();
        }else{
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithPriority();
        }

        List<String> result = new ArrayList<>();
        recommendations.forEach((one) -> {
            JSONObject json = JSONUtil.parseObj(one);
            result.add(json.toString());
        });

        redisTemplate.delete("flow_regulation");
        redisTemplate.opsForList().rightPushAll("flow_regulation", result.toArray(new String[0]));

        log.debug("更新了体检调流缓存");
    }
}
