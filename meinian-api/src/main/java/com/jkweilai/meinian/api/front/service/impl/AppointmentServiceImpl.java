package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.common.Constant;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.MedAppointmentLimitMapper;
import com.jkweilai.meinian.api.db.mapper.MedExamAppointmentMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.db.pojo.MedExamAppointmentEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.front.service.AppointmentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("FrontAppointmentServiceImpl")
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private MedExamAppointmentMapper medExamAppointmentMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private MedAppointmentLimitMapper medAppointmentLimitMapper;
    @Resource
    private RedisTemplate redisTemplate;
    private static Random random = new Random();

    @Override
    public String save(MedExamAppointmentEntity entity) {

        String key = Constant.APPOINTMENT_LIMIT_PREFIX + entity.getAppointmentDate();

        String execute = (String)redisTemplate.execute(new SessionCallback<String>() {

            @Override
            public String execute(RedisOperations operations) throws DataAccessException {
                operations.watch(key);
                Map entries = redisTemplate.opsForHash().entries(key);
                Integer actualCount = MapUtil.getInt(entries, "actualCount");
                Integer actualLimit = MapUtil.getInt(entries, "actualLimit");
                //预约数量 < 限制数量，可以预约
                if(actualCount < actualLimit) {
                    operations.multi();

                    redisTemplate.opsForHash().increment(key, "actualCount", 1);

                    //这里存放了每一条redis命令执行后的返回值
                    List exec = operations.exec();

                    if(!exec.isEmpty()){
                        return Constant.APPOINTMENT_SUCCESS;
                    }else{
                        return Constant.APPOINTMENT_FAIL;
                    }
                }else{
                    operations.unwatch();
                    return Constant.APPOINTMENT_FULL;
                }
            }
        });

        //可能是预约失败，也可能是今日预约已满请选择其他日期,直接返回就行
        if(!execute.equals(Constant.APPOINTMENT_SUCCESS)){
            return execute;
        }

        try{
            //执行到这里，证明预约成功了，redis 一定 + 1 了
            //那就要更新数据库了
            // 1.保存预约记录
            int rows = medExamAppointmentMapper.insert(entity);
            if(rows != 1){
                log.error("redis中的{}的actualCount值成功加1，单保存预约记录失败", key);
                throw new HisException("保存预约记录失败");
            }
            // 2.修改（保存）限流规则
            Map<String, Object> params = new HashMap<>();
            params.put("appointmentDate", entity.getAppointmentDate());
            Integer actualLimit = MapUtil.getInt(redisTemplate.opsForHash().entries(key), "actualLimit");
            params.put("actualLimit", actualLimit);
            params.put("maxLimit", actualLimit);
            rows = medAppointmentLimitMapper.saveOrUpdateAppointmentLimit(params);
            if(rows == 0){
                log.error("redis中的{}的actualCount值成功加1，保存预约记录成功，但更新/保存限流规则失败", key);
                throw new HisException("保存/更新限流规则失败");
            }
            // 3.修改订单状态
            Map<String, Object> mapArgs = new HashMap<>();
            mapArgs.put("orderStatus", 5);
            mapArgs.put("orderId", entity.getOrderId());
            rows = tradeOrderMapper.updateStatus(mapArgs);
            if(rows != 1){
                log.error("redis中的{}的actualCount值成功加1，保存预约记录成功，更新/保存限流规则成功，但更新订单状态失败", key);
                throw new HisException("更新订单状态失败，这里是将订单的状态更新为5：已预约，但失败了");
            }

            //执行到这里，更新数据库操作全部成功
            return Constant.APPOINTMENT_SUCCESS;
        }catch(Exception e){
            // 只要有异常，就手动回滚redis
            redisTemplate.execute(new SessionCallback<String>() {
                @Override
                public String execute(RedisOperations operations) throws DataAccessException {
                    // 重试机制（尽可能让redis回滚）
                    boolean rollbackSuccess = false;
                    for (int i = 0; i < 3 && !rollbackSuccess; i++) {
                        operations.watch(key);
                        operations.multi();
                        redisTemplate.opsForHash().increment(key, "actualCount", -1);
                        List exec = operations.exec();
                        if(!exec.isEmpty()){
                            // 回滚成功
                            rollbackSuccess = true;
                        }

                        // 连续执行回滚操作可能一直失败，随机睡眠一会儿
                        int time = random.nextInt(101);
                        try{
                            Thread.sleep(time);
                        }catch(InterruptedException ex){
                            throw new RuntimeException(ex);
                        }
                    }

                    // 补偿表机制
                    if(!rollbackSuccess){
                        // 将这个失败的操作记录到一个表当中，给负责人发短信。
                        // 人为的需要参与进来，手动去修改redis的缓存。
                    }

                    return null;
                }
            });

            // 既然已经出异常了，那就抛异常，不要吞没异常
            throw new HisException(e);
        }
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        List<Map<String, Object>> records = new ArrayList<>();
        long total = medExamAppointmentMapper.selectCountByQueryCondition(param);
        if(total > 0){
            records = medExamAppointmentMapper.selectPageByQueryCondition(param);
        }
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        return new PageResult<Map<String, Object>>(total, pageSize, pageNo, records);
    }


}
