package com.jkweilai.meinian.api.front.service;

import com.jkweilai.meinian.api.common.PageResult;

import java.util.Map;

public interface TradeOrderService {

    //生成微信支付单
    Map<String, Object> createPayment(Map<String, Object> params);

    //支付成功后，插入订单编号，修改订单状态3（已付款）
    boolean updatePayment(Map<String, Object> params);

    //清除redis中的付款单
    void clearRedisPayment(String outTradeNo);

    //通过订单流水号返回客户id
    Integer findCustomerIdByOutTradeNo(String outTradeNo);

    //获取支付结果
    boolean getPaymentResult(String outTradeNo);

    //分页查询我的订单
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //退款
    boolean refund(Map<String, Object> param);

    //退款完成后修改订单状态为4
    boolean modifyStatusByOutTradeNo(String outTradeNo);

    //判断用户是否有这个订单
    boolean hasOwnOrder(Map<String, Object> param);
}
