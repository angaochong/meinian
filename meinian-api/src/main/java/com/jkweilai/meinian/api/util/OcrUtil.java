package com.jkweilai.meinian.api.util;

import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;
import com.jkweilai.meinian.api.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class OcrUtil {
    @Value("${HUAWEI_OCR_APPKEY}")
    private String appKey;

    @Value("${HUAWEI_OCR_APPSECRET}")
    private String appSecret;

    public Map<String, Object> identifyWaybill(String imgBase64) throws Exception {
        Request request = new Request();
        request.setKey(appKey);
        request.setSecret(appSecret);
        request.setMethod("POST");
        request.setUrl("https://jmexpressbill.apistore.huaweicloud.com/ocr/express-bill");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //对上传的图片Base64字符串做URL编码
        //base64的字符串如果是作为请求参数传递的话，必须编码，因为base64中含有"=和/"等特殊字符。
        String encode = URLUtil.encodeAll(imgBase64);
        //在请求体中设置上传的图片
        request.setBody("base64=" + encode);
        //对请求中的内容生成数字签名
        HttpRequestBase signedRequest = Client.sign(request);
        //创建HTTP客户端对象(这是apache的httpclient库中的API，也可以使用hutool中的HttpUtil)
        CloseableHttpClient client = HttpClients.custom().build();
        //发出HTTP请求，并且得到返回的响应
        CloseableHttpResponse response = client.execute(signedRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            log.error("OCR识别运单失败", response.toString());
            throw new HisException("OCR识别运单失败");
        }
        //获取响应体中的内容
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            // EntityUtils是Apache HttpClient中的，将实体转换成字符串。
            String string = EntityUtils.toString(entity, "utf-8");
            JSONObject json = JSONUtil.parseObj(string);
            int code = json.getInt("code");
            String msg = json.getStr("msg");
            if (code == 200) {
                JSONObject data = json.getJSONObject("data");
                //收件人姓名
                String recName = data.getStr("recipient_name");
                //收件人电话
                String recPhone = data.getStr("recipient_phone");
                //快递运单编号
                String expressNo = data.getStr("waybill_number");
                Map<String, Object> map = new HashMap<>() {{
                    put("recName", recName);
                    put("recPhone", recPhone);
                    put("expressNo", expressNo);
                }};
                return map;
            } else {
                log.error("OCR运单识别失败", msg);
                throw new HisException("OCR识别运单失败");
            }
        } else {
            throw new HisException("OCR请求的响应体为空");
        }
    }
}
