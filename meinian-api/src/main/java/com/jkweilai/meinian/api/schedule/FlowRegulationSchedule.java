package com.jkweilai.meinian.api.schedule;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.db.mapper.MedDepartmentFlowMapper;
import com.jkweilai.meinian.api.exception.HisException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
public class FlowRegulationSchedule {
    @Resource
    private MedDepartmentFlowMapper medDepartmentFlowMapper;

    @Resource
    private RedisTemplate redisTemplate;

    //@Scheduled(cron = "0 * 7-16 * * ?") // 生产环境下的建议：每天7-16点之间，每分钟计算一次推荐的科室列表
    @Scheduled(cron = "0,20,40 * * * * ?") // 测试的时候每隔20秒执行一次
    @Transactional
    public void refreshFlowRegulation() {
        //查询所有调流科室
        List<Map<String, Object>> deptList = medDepartmentFlowMapper.selectAllDept();
        //所有体检人的排队缓存
        Set<String> keys = redisTemplate.keys("flow_regulation_customer#*");

        //遍历所有体检调流科室
        deptList.forEach(one -> {
            int id = Integer.parseInt(one.get("id").toString());
            String departmentName = one.get("departmentName").toString();
            int sum = 0;
            //遍历所有体检人缓存，如果是在当前科室排队，就累加sum值
            for (String key : keys) {
                ValueOperations ops = redisTemplate.opsForValue();
                int deptId = Integer.parseInt(ops.get(key).toString());
                if (id == deptId) {
                    sum++;
                }
            }
            //把该调流科室排队人数更新到数据库
            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            param.put("currentCount", sum);

            int rows = medDepartmentFlowMapper.updateCurrentCount(param);
            if (rows != 1) {
                throw new HisException("更新体检调流排队人数失败");
            }
        });


        //查询当前的调流模式
        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation").toString();
        boolean mode = Boolean.parseBoolean(value);
        List<Map<String, Object>> recommendations = null;

        if (mode) {
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithWeight();
        } else {
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