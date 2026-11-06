package com.jkweilai.meinian.api.schedule;

import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderSchedule {

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    //格式：秒 分 时 日 月 星期
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void closeOrder(){
        int rows = tradeOrderMapper.closeOrder();
        if(rows > 0){
            log.info("关闭了{}个未支付且超时的订单", rows);
        }
    }
}
