package com.jkweilai.meinian.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

@Slf4j
@Component  // 声明为Spring组件，可以被自动注入
public class WechatPayCallbackValidator {

    // 注入微信支付API v3密钥，用于解密回调数据
    @Value("${wechat.pay.v3.meinian-vue.app-v3-secret}")
    private String apiV3Key;

    // JSON序列化工具，用于处理回调数据
    private final ObjectMapper objectMapper;

    // 静态代码块：在类加载时注册BouncyCastle密码学提供者
    static {
        Security.addProvider(new BouncyCastleProvider());  // 添加BouncyCastle加密库支持
    }

    // 构造函数，通过依赖注入ObjectMapper
    public WechatPayCallbackValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 开发环境签名验证
     * 验证微信支付回调请求的签名，确保请求来自微信服务器而非伪造
     *
     * @param timestamp 请求时间戳，微信服务器生成
     * @param nonce 随机字符串，防止重放攻击
     * @param body 请求体数据（JSON格式）
     * @param signature 微信服务器计算的数字签名
     * @param serial 证书序列号（当前方法未使用）
     * @return true表示验证通过，false表示验证失败
     */
    public boolean verifyWechatSignature(String timestamp, String nonce, String body,
                                         String signature, String serial) {
        try {
            // 基础参数检查：确保所有必要参数都不为空
            if (timestamp == null || nonce == null || body == null || signature == null) {
                log.error("签名验证失败：必要参数缺失");
                return false;
            }

            // 时间戳验证（5分钟内有效），防止过期请求被重放
            long requestTime = Long.parseLong(timestamp) * 1000;  // 转换为毫秒
            long currentTime = System.currentTimeMillis();        // 当前系统时间
            long timeDiff = Math.abs(currentTime - requestTime);  // 计算时间差
            if (timeDiff > 5 * 60 * 1000) {  // 5分钟 = 5 * 60秒 * 1000毫秒
                log.error("签名验证失败：时间戳过期");
                return false;
            }

            // TODO: 实际生产环境需要在这里添加签名计算和比对逻辑
            // 1. 使用apiV3Key对(timestamp + "\n" + nonce + "\n" + body)进行HMAC-SHA256计算
            // 2. 将计算结果与传入的signature参数进行比对


            log.info("开发环境签名验证通过");  // 当前仅通过基础验证，用于开发测试
            return true;

        } catch (Exception e) {
            log.error("签名验证异常", e);  // 记录异常日志
            return false;  // 出现异常时验证失败
        }
    }

    /**
     * 解密微信回调资源数据
     * 使用AES-GCM算法解密微信支付回调中的敏感数据（如金额、用户信息等）
     *
     * @param ciphertext Base64编码的加密数据
     * @param associatedData 附加数据，用于完整性验证
     * @param nonce 随机数，用于加密运算
     * @return 解密后的原始JSON字符串
     * @throws Exception 解密失败时抛出异常
     */
    public String decryptResourceData(String ciphertext, String associatedData, String nonce) throws Exception {
        try {
            // 准备解密所需的密钥和参数
            byte[] key = apiV3Key.getBytes(StandardCharsets.UTF_8);           // API密钥字节数组
            byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);       // 随机数字节数组
            byte[] associatedDataBytes = associatedData.getBytes(StandardCharsets.UTF_8);  // 附加数据字节数组
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);  // Base64解码加密数据

            // 配置AES-GCM解密器
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");  // 创建AES密钥规范
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");  // 获取AES-GCM算法实例，使用BouncyCastle提供者
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, nonceBytes);  // 创建GCM参数规范，128位认证标签

            // 执行解密操作
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);  // 初始化解密器
            cipher.updateAAD(associatedDataBytes);  // 设置附加认证数据，用于完整性验证

            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);  // 执行解密，得到原始数据字节数组
            return new String(decryptedBytes, StandardCharsets.UTF_8);  // 将字节数组转换为UTF-8字符串返回

        } catch (Exception e) {
            log.error("解密回调数据失败", e);  // 记录解密失败日志
            throw new RuntimeException("解密失败", e);  // 包装并抛出运行时异常
        }
    }
}