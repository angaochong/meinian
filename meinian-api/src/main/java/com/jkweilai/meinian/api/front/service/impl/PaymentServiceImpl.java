package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jkweilai.meinian.api.front.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service("FrontPaymentServiceImpl")
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    // 从配置文件中注入微信支付商户号
    @Value("${wechat.pay.v3.meinian-vue.mch-id}")
    private String mchId;

    // 从配置文件中注入微信支付应用ID
    @Value("${wechat.pay.v3.meinian-vue.app-id}")
    private String appId;

    // 微信支付HTTP客户端，自动处理签名和认证
    private CloseableHttpClient httpClient;

    // JSON序列化工具
    private final ObjectMapper objectMapper;

    /**
     * 构造函数，通过依赖注入初始化HTTP客户端和JSON工具
     * @param wechatPayHttpClient 微信支付专用的HTTP客户端（自动配置签名等）
     * @param objectMapper JSON序列化工具
     */
    public PaymentServiceImpl(CloseableHttpClient wechatPayHttpClient, ObjectMapper objectMapper) {
        this.httpClient = wechatPayHttpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 微信支付统一下单接口 - Native支付（扫码支付）
     * 调用微信支付V3接口创建支付订单，返回包含支付二维码链接的响应
     *
     * @param outTradeNo 商户订单号，商户系统内部订单号，要求32个字符内
     * @param total 订单总金额，单位：分
     * @param desc 商品描述，如："腾讯充值中心-QQ会员充值"
     * @param notifyUrl 支付结果通知URL，微信服务器会主动发送支付结果到此URL
     * @param timeExpire 订单过期时间，ISO8601格式，如："2025-10-26T12:00:00+08:00"
     * @return ObjectNode 微信支付API响应数据，包含code_url（二维码链接）等字段
     */
    @Override
    public ObjectNode unifiedOrder(String outTradeNo, int total, String desc, String notifyUrl, String timeExpire) {
        try {
            // 构建请求参数Map，对应微信支付API要求的JSON结构
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", appId);              // 应用ID
            requestMap.put("mchid", mchId);              // 商户号
            requestMap.put("description", desc);         // 商品描述
            requestMap.put("out_trade_no", outTradeNo);  // 商户订单号
            requestMap.put("notify_url", notifyUrl);     // 支付结果回调地址

            // 构建金额信息对象
            Map<String, Object> amountMap = new HashMap<>();
            // amountMap.put("total", total); // 实际金额（单位：分）
            amountMap.put("total", 1); // 测试金额：1分钱（测试环境使用，生产环境需要注释）
            amountMap.put("currency", "CNY"); // 货币类型：人民币
            requestMap.put("amount", amountMap);

            // 处理订单过期时间（可选参数）
            if (timeExpire != null) {
                // 将字符串时间解析为OffsetDateTime，然后格式化为ISO8601标准格式
                // OffsetDateTime 是Java8中提供的，该对象包含日期时间信息的同时还包含时区的信息。
                // 微信支付要求过期时间信息必须带有时区信息。
                OffsetDateTime expireTime = OffsetDateTime.parse(timeExpire);
                requestMap.put("time_expire", expireTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }

            // 创建HTTP POST请求到微信支付Native下单接口
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/native");
            httpPost.addHeader("Accept", "application/json"); // 接受JSON响应
            httpPost.addHeader("Content-type", "application/json; charset=utf-8"); // 发送JSON请求体
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"); // 浏览器标识

            // 将请求参数Map转换为JSON字符串
            String requestBody = objectMapper.writeValueAsString(requestMap);
            // 设置请求体，指定UTF-8编码防止中文乱码
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));

            // 记录请求日志（生产环境建议对敏感信息脱敏）
            log.info("调用微信支付Native下单，请求参数：{}", requestBody);

            // 执行HTTP请求，使用try-with-resources确保响应流正确关闭
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 读取响应体内容
                String responseBody = EntityUtils.toString(response.getEntity());
                // 获取HTTP状态码
                int statusCode = response.getStatusLine().getStatusCode();

                log.info("微信支付响应：状态码={}", statusCode);

                // 处理成功响应（状态码200）
                if (statusCode == 200) {
                    // 将一个 JSON 字符串解析为可操作的 JSON 对象
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    ObjectNode result = objectMapper.createObjectNode();
                    // 如果响应是JSON对象，将其所有字段复制到结果中
                    if (jsonNode.isObject()) {
                        result.setAll((ObjectNode) jsonNode);
                    }
                    return result; // 返回包含code_url等支付信息的JSON对象
                } else {
                    // 处理失败响应，记录错误日志并抛出异常
                    log.error("创建微信支付订单失败，状态码：{}，响应：{}", statusCode, responseBody);
                    throw new RuntimeException("创建微信支付订单失败，状态码：" + statusCode);
                }
            }

        } catch (Exception e) {
            // 捕获所有异常，记录错误日志并包装为运行时异常抛出
            log.error("调用微信支付Native下单API异常", e);
            throw new RuntimeException("创建微信支付订单失败：" + e.getMessage());
        }
    }

    @Override
    public String getPaymentResult(String outTradeNo) {
        try {
            // 构建查询请求
            HttpGet httpGet = new HttpGet("https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + mchId);
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            log.info("查询微信支付结果，订单号：{}", outTradeNo);

            // 执行查询请求
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getStatusLine().getStatusCode();

                log.info("微信支付查询响应：状态码={}", statusCode);

                if (statusCode == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String tradeState = jsonNode.get("trade_state").asText();

                    if ("SUCCESS".equals(tradeState)) {
                        String transactionId = jsonNode.get("transaction_id").asText();
                        log.info("订单 {} 支付成功，微信支付订单号：{}", outTradeNo, transactionId);
                        return transactionId;
                    } else {
                        log.info("订单 {} 支付状态：{}", outTradeNo, tradeState);
                        return null;
                    }
                } else {
                    log.error("查询微信支付结果失败，状态码：{}，响应：{}", statusCode, responseBody);
                    return null;
                }
            }

        } catch (Exception e) {
            log.error("查询微信支付结果异常，订单号：{}", outTradeNo, e);
            return null;
        }
    }

    @Override
    public String refund(String transactionId, Integer refund, Integer total, String notifyUrl) {
        try {
            // 生成退款流水号
            String outRefundNo = IdUtil.simpleUUID().toUpperCase();

            // 构建退款请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("transaction_id", transactionId); // 微信支付订单号
            requestMap.put("out_refund_no", outRefundNo);   // 商户退款单号
            requestMap.put("notify_url", notifyUrl);        // 退款结果通知URL

            // 构建金额信息
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("refund", refund);                // 退款金额
            amountMap.put("total", total);                  // 原订单金额
            amountMap.put("currency", "CNY");               // 币种
            requestMap.put("amount", amountMap);

            // 创建HTTP POST请求到微信支付退款接口
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 设置请求体
            String requestBody = objectMapper.writeValueAsString(requestMap);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));

            log.info("调用微信支付退款接口，请求参数：{}", requestBody);

            // 执行退款请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getStatusLine().getStatusCode();

                log.info("微信支付退款响应：状态码={}, 响应体={}", statusCode, responseBody);

                if (statusCode == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String status = jsonNode.get("status").asText();

                    // 判断退款状态
                    if ("PROCESSING".equals(status)) {
                        log.info("退款申请成功，退款单号：{}", outRefundNo);
                        return outRefundNo;
                    } else {
                        log.warn("退款状态异常，状态：{}，退款单号：{}", status, outRefundNo);
                        return null;
                    }
                } else {
                    log.error("微信支付退款失败，状态码：{}，响应：{}", statusCode, responseBody);
                    return null;
                }
            }

        } catch (Exception e) {
            log.error("调用微信支付退款API异常", e);
            return null;
        }
    }
}