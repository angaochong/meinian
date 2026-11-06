package com.jkweilai.meinian.api.app.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.app.service.FlowRegulationService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("AppFlowRegulationServiceImpl")
public class FlowRegulationServiceImpl implements FlowRegulationService {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<Object> findRecommendations() {
        List<String> list = redisTemplate.opsForList().range("flow_regulation", 0, -1);
        List<Object> result = new ArrayList<>();
        list.forEach(item -> {
            JSONObject json = JSONUtil.parseObj(item.toString());
            result.add(json);
        });
        return result;
    }
}
