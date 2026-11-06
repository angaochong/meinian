package com.jkweilai.meinian.api.front.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PaymentService {
    // 统一下单 方法
    // outTradeNo：咱们自己系统生成的唯一订单号，用来标识这笔交易是咱们系统中的“哪一单”。
    // total：订单总金额，单位是分（100分 = 1元），要付50元就传5000。
    // desc：商品描述信息，会显示在用户的微信支付账单里。
    // notifyUrl：微信支付成功后，微信会通过这个url告诉你“钱已到账”。
    // timeExpire：订单的最晚支付时间，超过这个时间订单就自动失效
    public ObjectNode unifiedOrder(String outTradeNo, int total,
                                   String desc, String notifyUrl,
                                   String timeExpire);

    //获取支付结果
    String getPaymentResult(String outTradeNo);

    /**
     * 退款方法
     * @param transactionId 付款单id
     * @param refund 退款金额
     * @param total 订单总金额
     * @param notifyUrl 回调url
     * @return 退款状态是 PROCESSING时，立即返回退款单号 out_refund_no，否则返回null。
     */
    String refund(String transactionId, Integer refund, Integer total, String notifyUrl);
}
