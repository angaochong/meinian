package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.db.mapper.SysConfigMapper;
import com.jkweilai.meinian.api.mis.service.SysConfigService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.jkweilai.meinian.api.common.Constant.AUTO_FLOW_REGULATION_PREFIX;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Override
    public String getItemValue(String key) {
        return redisTemplate.opsForValue().get(AUTO_FLOW_REGULATION_PREFIX + key).toString();
    }

    @Override
    @Transactional
    public boolean setConfigValue(String configKey, String configValue) {
        Map<String,String> map = new HashMap<>();
        map.put("configKey",configKey);
        map.put("configValue",configValue);

        int rows = sysConfigMapper.updateByConfigKey(map);
        if(rows > 0){
            redisTemplate.opsForValue().set(AUTO_FLOW_REGULATION_PREFIX +  configKey,configValue);
            return true;
        }
        return false;
    }
}
