package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.jkweilai.meinian.api.db.mapper.CrmCustomerMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.db.pojo.CrmCustomerEntity;
import com.jkweilai.meinian.api.front.service.CrmCustomerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service("FrontCrmCustomerServiceImpl")
@Slf4j
public class CrmCustomerServiceImpl implements CrmCustomerService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CrmCustomerMapper crmCustomerMapper;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Override
    public boolean sendSmsCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        log.info("验证码是：" + code);
        if(redisTemplate.hasKey("sms_code_refresh_" + phone)){
            return false;
        }

        redisTemplate.opsForValue().set("sms_code_refresh_" + phone, code);
        redisTemplate.expire("sms_code_refresh_" + phone, 1, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set("sms_code_" + phone, code);
        redisTemplate.expire("sms_code_" + phone, 5, TimeUnit.MINUTES);

        return true;
    }

    @Override
    @Transactional
    public Map<String, Object> login(String phone, String code) {

        Map<String, Object> map = new HashMap<>();

        String key = "sms_code_" + phone;
        //redis中没有key就是过期的
        if(!redisTemplate.hasKey(key)){
            map.put("result", false);
            map.put("msg", "短信验证码已过期");
            return map;
        }

        //验证码是否正确
        String smsCode = redisTemplate.opsForValue().get(key).toString();
        if(!smsCode.equals(code)){
            map.put("result", false);
            map.put("msg", "短信验证码错误");
            return map;
        }

        // 程序执行到这里说明手机号和验证码都有效。先清空redis中的两个key
        redisTemplate.delete(key);
        redisTemplate.delete("sms_code_refresh_" + phone);

        //判断该用户是否已经注册
        Integer id = crmCustomerMapper.selectByPhone(phone);
        //数据库没有id，证明没有这个用户，所以要insert用户
        if(id == null){
            CrmCustomerEntity crmCustomerEntity = new CrmCustomerEntity();
            crmCustomerEntity.setPhone(phone);
            crmCustomerMapper.insert(crmCustomerEntity);
            id = crmCustomerEntity.getId();
        }

        map.put("result", true);
        map.put("id", id);
        map.put("msg", "登录成功");

        return map;
    }

    @Override
    public Map<String, Object> getSummaryById(Integer id) {
        Map<String, Object> map = crmCustomerMapper.selectById(id);
        map.putAll(tradeOrderMapper.selectOrderStatistics(id));
        return map;
    }

    @Override
    @Transactional
    public boolean modify(Map<String, Object> param) {
        return crmCustomerMapper.update(param) == 1;
    }

    @Override
    public boolean phoneExist(String phone, Integer id) {
        return crmCustomerMapper.countPhoneExist(phone, id) == 1;
    }
}
