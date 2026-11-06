package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;

import java.util.Map;

public interface TradeOrderService {

    //分页查询订单模块
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //同步付款结果
    int syncPaymentResult(String[] outTradeNos);

    //删除订单记录
    int removeById(Integer id);

    //线下退款更新订单状态
    boolean processOfflineRefund(String outTradeNo);
}
