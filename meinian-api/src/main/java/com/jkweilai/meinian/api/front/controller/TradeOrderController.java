package com.jkweilai.meinian.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import com.jkweilai.meinian.api.front.controller.form.CreatePaymentForm;
import com.jkweilai.meinian.api.front.controller.form.GetPaymentResultForm;
import com.jkweilai.meinian.api.front.controller.form.OrderPageQueryForm;
import com.jkweilai.meinian.api.front.controller.form.RefundForm;
import com.jkweilai.meinian.api.front.service.TradeOrderService;
import com.jkweilai.meinian.api.socket.MessagePushEndpoint;
import com.jkweilai.meinian.api.util.WechatPayCallbackValidator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("FrontTradeOrderController")
@RequestMapping("/front/order")
@Slf4j
public class TradeOrderController {

    @Resource(name = "FrontTradeOrderServiceImpl")
    private TradeOrderService tradeOrderService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WechatPayCallbackValidator wechatPayCallbackValidator;

    @PostMapping("/createPayment")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R createPayment(@RequestBody @Valid CreatePaymentForm form){
        //把顾客id，商品id，商品数量 放到 map 中，生成微信支付单
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("customerId", customerId);
        Map<String, Object> result = tradeOrderService.createPayment(param);
        if(result == null){
            return R.ok().put("illegal", true);
        }else{
            return R.ok().put("illegal", false).put("result", result);
        }
    }

    //忽略异常
    @SneakyThrows
    @PostMapping("/paymentCallback")
    public Map<String, String> paymentCallback(
            @RequestHeader("Wechatpay-Serial") String serial,           // 微信支付证书序列号，用于验证签名
            @RequestHeader("Wechatpay-Signature") String signature,     // 微信支付请求签名，用于验证消息真实性
            @RequestHeader("Wechatpay-Timestamp") String timestamp,     // 请求时间戳，用于防止重放攻击
            @RequestHeader("Wechatpay-Nonce") String nonce,             // 随机字符串，用于防止重放攻击
            HttpServletRequest request) {                               // HTTP请求对象，用于读取请求体

        Map<String, String> response = new HashMap<>();  // 创建响应Map，用于返回给微信支付服务器

        try {
            // 读取请求体：将HTTP请求的输入流按行读取并拼接成完整字符串
            String body = request.getReader().lines().collect(Collectors.joining());
            log.info("收到微信支付回调");  // 记录回调接收日志

            // 1. 生产环境签名验证：验证回调请求是否确实来自微信支付服务器
            if (!wechatPayCallbackValidator.verifyWechatSignature(timestamp, nonce, body, signature, serial)) {
                log.error("微信支付回调签名验证失败");  // 签名验证失败日志
                response.put("code", "FAIL");        // 设置响应码为失败
                response.put("message", "签名验证失败");  // 设置响应消息
                return response;  // 立即返回失败响应，不继续处理
            }

            // 2. 解析回调数据：将请求体JSON字符串解析为JsonNode对象便于操作
            JsonNode callbackData = objectMapper.readTree(body);
            JsonNode resource = callbackData.get("resource");  // 获取加密的资源数据节点

            // 提取加密数据所需的三个参数
            String ciphertext = resource.get("ciphertext").asText();          // Base64编码的加密数据
            String associatedData = resource.get("associated_data").asText(); // 附加认证数据
            String resourceNonce = resource.get("nonce").asText();            // 加密随机数

            // 3. 解密资源数据：使用AES-GCM算法解密微信支付回调中的敏感信息
            String decryptedData = wechatPayCallbackValidator.decryptResourceData(
                    ciphertext, associatedData, resourceNonce
            );
            // 将解密后的JSON字符串再次解析为JsonNode对象
            JsonNode decryptedJson = objectMapper.readTree(decryptedData);

            // 从解密后的数据中提取关键业务字段
            String outTradeNo = decryptedJson.get("out_trade_no").asText();      // 商户订单号
            String transactionId = decryptedJson.get("transaction_id").asText(); // 微信支付订单号
            String tradeState = decryptedJson.get("trade_state").asText();       // 交易状态

            log.info("支付回调解密成功: outTradeNo={}, tradeState={}", outTradeNo, tradeState);  // 记录解密成功日志

            // 4. 处理支付成功订单：只有当交易状态为"SUCCESS"时才更新订单
            if ("SUCCESS".equals(tradeState)) {
                Map<String, Object> param = new HashMap<>();  // 创建参数Map用于数据库更新
                param.put("transactionId", transactionId);    // 微信支付订单号
                param.put("outTradeNo", outTradeNo);          // 商户订单号

                // 调用服务层方法更新订单支付状态
                boolean success = tradeOrderService.updatePayment(param);
                log.info("订单更新{}: {}", success ? "成功" : "失败", outTradeNo);  // 记录订单更新结果
                if(success){
                    //清除redis中的付款单
                    tradeOrderService.clearRedisPayment(outTradeNo);
                    // 返回成功响应给微信支付服务器
                    response.put("code", "SUCCESS");  // 必须返回SUCCESS，否则微信会重复发送回调
                    response.put("message", "OK");    // 成功消息
                }
                Integer customerId = tradeOrderService.findCustomerIdByOutTradeNo(outTradeNo);
                if(customerId != null){
                    JSONObject json = new JSONObject();
                    json.set("result", true);
                    MessagePushEndpoint.sendInfo(json.toString(), "customer_" + customerId);
                }
            }
            return response;
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);  // 记录异常详细信息
            response.put("code", "FAIL");     // 设置失败响应码
            response.put("message", "处理失败");  // 设置失败消息
            return response;  // 返回失败响应
        }
    }

    @PostMapping("/paymentResult")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R paymentResult(@RequestBody @Valid GetPaymentResultForm form){
        String outTradeNo = form.getOutTradeNo();
        boolean paymentResult = tradeOrderService.getPaymentResult(outTradeNo);
        return R.ok().put("result", paymentResult);
    }

    @PostMapping("/pageQuery")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R pageQuery(@RequestBody @Valid OrderPageQueryForm form){
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        param.put("customerId", customerId);
        PageResult<Map<String, Object>> pageResult = tradeOrderService.pageQueryByCondition(param);
        return R.ok().put("result", pageResult);
    }

    @PostMapping("/refund")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R refund(@RequestBody @Valid RefundForm form){
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("customerId", StpCustomerUtil.getLoginIdAsInt());

        boolean result = tradeOrderService.refund(params);

        return R.ok().put("result", result);
    }

    @SneakyThrows
    @PostMapping("/refundCallback")
    public Map<String, String> refundCallback(
            @RequestHeader("Wechatpay-Serial") String serial,           // 微信支付证书序列号，用于验证签名
            @RequestHeader("Wechatpay-Signature") String signature,     // 微信支付请求签名，用于验证消息真实性
            @RequestHeader("Wechatpay-Timestamp") String timestamp,     // 请求时间戳，用于防止重放攻击
            @RequestHeader("Wechatpay-Nonce") String nonce,             // 随机字符串，用于防止重放攻击
            HttpServletRequest request) {                               // HTTP请求对象，用于读取请求体

        Map<String, String> response = new HashMap<>();  // 创建响应Map，用于返回给微信支付服务器

        try {
            // 读取请求体：将HTTP请求的输入流按行读取并拼接成完整字符串
            String body = request.getReader().lines().collect(Collectors.joining());
            log.info("收到微信退款回调");  // 记录回调接收日志

            // 1. 生产环境签名验证：验证回调请求是否确实来自微信支付服务器
            if (!wechatPayCallbackValidator.verifyWechatSignature(timestamp, nonce, body, signature, serial)) {
                log.error("微信退款回调签名验证失败");  // 签名验证失败日志
                response.put("code", "FAIL");        // 设置响应码为失败
                response.put("message", "签名验证失败");  // 设置响应消息
                return response;  // 立即返回失败响应，不继续处理
            }

            // 2. 解析回调数据：将请求体JSON字符串解析为JsonNode对象便于操作
            JsonNode callbackData = objectMapper.readTree(body);
            JsonNode resource = callbackData.get("resource");  // 获取加密的资源数据节点

            // 提取加密数据所需的三个参数
            String ciphertext = resource.get("ciphertext").asText();          // Base64编码的加密数据
            String associatedData = resource.get("associated_data").asText(); // 附加认证数据
            String resourceNonce = resource.get("nonce").asText();            // 加密随机数

            // 3. 解密资源数据：使用AES-GCM算法解密微信支付回调中的敏感信息
            String decryptedData = wechatPayCallbackValidator.decryptResourceData(
                    ciphertext, associatedData, resourceNonce
            );
            // 将解密后的JSON字符串再次解析为JsonNode对象
            JsonNode decryptedJson = objectMapper.readTree(decryptedData);

            // 从解密后的数据中提取关键业务字段
            String outRefundNo = decryptedJson.get("out_refund_no").asText();    // 商户退款单号
            String refundStatus = decryptedJson.get("refund_status").asText();   // 退款状态
            String outTradeNo = decryptedJson.get("out_trade_no").asText();
            String successTime = decryptedJson.has("success_time") ?
                    decryptedJson.get("success_time").asText() : null; // 退款成功时间（如果有）

            log.info("退款回调解密成功: outRefundNo={}, refundStatus={}", outRefundNo, refundStatus);  // 记录解密成功日志

            // 4. 处理退款回调业务逻辑
            if ("SUCCESS".equals(refundStatus)) {
                // 退款成功：更新订单状态为已退款
                boolean success = tradeOrderService.modifyStatusByOutTradeNo(outTradeNo);
                if (success) {
                    log.info("退款成功，退款单号：{}", outRefundNo);

                } else {
                    log.error("退款回调成功，但订单状态更新失败，退款单号：{}", outRefundNo);
                }
            } else if ("ABNORMAL".equals(refundStatus)) {
                // 退款异常：用户银行卡作废或者冻结
                log.warn("退款状态异常，退款单号：{}，可能原因：用户银行卡作废或冻结", outRefundNo);
            } else if ("CLOSED".equals(refundStatus)) {
                // 退款关闭
                log.info("退款已关闭，退款单号：{}", outRefundNo);
            } else {
                // 其他状态（如PROCESSING等）
                log.info("退款处理中，退款单号：{}，状态：{}", outRefundNo, refundStatus);
            }

            // 返回成功响应给微信支付服务器
            response.put("code", "SUCCESS");  // 必须返回SUCCESS，否则微信会重复发送回调
            response.put("message", "OK");    // 成功消息
            return response;

        } catch (Exception e) {
            log.error("处理微信退款回调异常", e);  // 记录异常详细信息
            response.put("code", "FAIL");     // 设置失败响应码
            response.put("message", "处理失败");  // 设置失败消息
            return response;  // 返回失败响应
        }
    }
}
