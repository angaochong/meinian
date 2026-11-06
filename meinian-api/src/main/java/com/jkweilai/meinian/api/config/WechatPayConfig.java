package com.jkweilai.meinian.api.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration  // 声明这是一个Spring配置类
public class WechatPayConfig {

    // 注入微信支付API v3密钥，用于敏感数据加密和验证
    @Value("${wechat.pay.v3.meinian-vue.app-v3-secret}")
    private String apiV3Key;

    /**
     * 创建微信支付专用的HTTP客户端Bean
     * 该客户端会自动处理请求签名、响应验证和证书更新
     *
     * @param mchId 商户号，从配置文件中注入
     * @param merchantSerialNo 商户证书序列号，从配置文件中注入
     */
    @Bean
    public CloseableHttpClient wechatPayHttpClient(
            @Value("${wechat.pay.v3.meinian-vue.mch-id}") String mchId,
            @Value("${wechat.pay.v3.meinian-vue.merchant-serial-no}") String merchantSerialNo) throws Exception {

        Resource resource = new ClassPathResource("apiclient_key.pem");
        String privateKeyStr = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        privateKeyStr = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyStr);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);


        // 创建自动更新证书的验证器，定期从微信服务器下载和更新平台证书
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                // 创建微信支付凭证，包含商户身份信息和签名器
                new WechatPay2Credentials(mchId, new PrivateKeySigner(merchantSerialNo, privateKey)),
                // API v3密钥转换为字节数组，用于响应验证
                apiV3Key.getBytes(StandardCharsets.UTF_8)
        );

        // 构建微信支付专用的HTTP客户端
        return WechatPayHttpClientBuilder.create()
                // 配置商户信息：商户号、证书序列号、私钥
                .withMerchant(mchId, merchantSerialNo, privateKey)
                // 添加验证器，用于验证微信支付响应的签名，确保响应未被篡改
                .withValidator(new WechatPay2Validator(verifier))  // 必须添加验证器
                .build();  // 构建最终的HTTP客户端实例
    }
}