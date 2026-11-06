package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.db.mapper.CrmCustomerImMapper;
import com.jkweilai.meinian.api.db.mapper.CrmCustomerMapper;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.front.service.CustomerImService;
import com.tencentyun.TLSSigAPIv2;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Service("FrontCustomerImServiceImpl")
@Slf4j
public class CustomerImServiceImpl implements CustomerImService {
    // 腾讯IM SDK应用ID
    @Value("${tencent.im.sdkAppId}")
    private Long sdkAppId;

    // 腾讯IM密钥
    @Value("${tencent.im.secretKey}")
    private String secretKey;

    // 管理员账号ID
    @Value("${tencent.im.managerId}")
    private String managerId;

    // 客服账号ID
    @Value("${tencent.im.customerServiceId}")
    private String customerServiceId;

    @Resource
    private CrmCustomerMapper crmCustomerMapper;

    @Resource
    private CrmCustomerImMapper crmCustomerImMapper;

    // 腾讯IM API基础地址
    private String baseUrl = "https://console.tim.qq.com/";

    @Override
    @Transactional
    public Map<String, Object> createAccount(int customerId) {
        // 从大健康系统中查询客户的信息
        Map<String, Object> map = crmCustomerMapper.selectById(customerId);
        String phone = MapUtil.getStr(map, "phone");
        String photoUrl = MapUtil.getStr(map, "photoUrl");
        // 生成客户IM账号，格式：customer_客户ID
        String account = "customer_" + customerId;
        // 生成客户昵称，格式：客户_手机号
        String nickname = "客户_" + phone;

        // 初始化腾讯IM签名生成器
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);

        // 客户在登录大健康IM系统时，我们后端生成这个签名给客户。
        // 客户拿着这个签名，就能直接进入腾讯IM的“俱乐部”聊天、收消息
        // 生成客户账号签名，有效期180天
        String userSigForCustomer = api.genUserSig(account, 180 * 86400);

        //保存返回的结果（最终result是需要返回给前端项目的）
        Map<String, Object> result = new HashMap();
        result.put("sdkAppId", sdkAppId);
        result.put("account", account);
        result.put("userSig", userSigForCustomer);

        // 重新生成管理员账号签名，用于后续API调用
        String userSig = api.genUserSig(managerId, 180 * 86400);

        // 构建查询账号状态的URL
        String url = baseUrl + "v4/im_open_login_svc/account_check?sdkappid=" + sdkAppId + "&identifier=" + managerId + "&usersig=" + userSig + "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";

        // 构建查询请求体
        JSONObject json = new JSONObject();
        json.set("CheckItem", new ArrayList<>() {{
            add(new HashMap<>() {{
                put("UserID", account); // 要检查的客户账号
            }});
        }});

        // 发送HTTP POST请求查询账号状态
        String response = HttpUtil.post(url, json.toString());
        JSONObject entries = JSONUtil.parseObj(response);
        Integer errorCode = entries.getInt("ErrorCode");
        String errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("查询客户IM账号失败：" + errorInfo);
            throw new HisException("客服系统异常");
        }

        // 解析查询结果
        JSONArray list = (JSONArray) entries.get("ResultItem");
        JSONObject object = (JSONObject) list.get(0);
        String accountStatus = object.getStr("AccountStatus");
        // 如果客户IM账号不存在
        if (!"Imported".equals(accountStatus)) {
            // 构建创建账号的URL
            url = baseUrl + "v4/im_open_login_svc/account_import?sdkappid=" + sdkAppId + "&identifier=" + managerId + "&usersig=" + userSig + "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";

            // 构建创建账号请求体
            json = new JSONObject();
            json.set("UserID", account); // 客户账号
            json.set("Nick", nickname);  // 客户昵称
            if (photoUrl != null) {
                json.set("FaceUrl", photoUrl); // 客户头像URL
            }

            // 发送HTTP POST请求创建IM账号
            response = HttpUtil.post(url, json.toString());
            entries = JSONUtil.parseObj(response);
            errorCode = entries.getInt("ErrorCode");
            errorInfo = entries.getStr("ErrorInfo");
            if (errorCode != 0) {
                log.error("创建客户IM账号失败：" + errorInfo);
                throw new HisException("客服系统异常");
            }

            //给客户IM账号添加客服好友
            // 构建添加好友的REST API URL，使用管理员身份调用腾讯IM添加好友接口
            url = baseUrl + "v4/sns/friend_add?sdkappid=" + sdkAppId + "&identifier=" + managerId + "&usersig=" + userSig + "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";

            // 构建添加好友的请求体JSON对象
            json = new JSONObject();
            json.set("From_Account", account); // 设置发起好友请求的客户账号
            json.set("AddFriendItem", new ArrayList<>() {{
                add(new HashMap() {{
                    put("To_Account", customerServiceId); // 设置要添加的好友账号（客服账号）
                    put("AddSource", "AddSource_Type_Web"); // 设置好友来源为Web端
                }});
            }});

            // 发送HTTP POST请求执行添加好友操作
            response = HttpUtil.post(url, json.toString());
            entries = JSONUtil.parseObj(response);
            errorCode = entries.getInt("ErrorCode");
            errorInfo = entries.getStr("ErrorInfo");

            // 检查API调用是否成功（HTTP层面和基础验证）
            if (errorCode != 0) {
                log.error("添加客服IM好友失败:" + errorInfo);
                throw new HisException("客服系统异常");
            }

            // 解析添加好友的具体结果
            list = (JSONArray) entries.get("ResultItem");
            object = (JSONObject) list.get(0);

            // 获取单个好友添加操作的结果代码和信息
            int resultCode = object.getInt("ResultCode");
            String resultInfo = object.getStr("ResultInfo");

            // 检查具体的好友添加操作是否成功
            if (resultCode != 0) {
                log.error("添加客服IM好友失败：" + resultInfo);
                throw new HisException("客服系统异常");
            }
        }

        //TODO 记录登录时间
        int rows = crmCustomerImMapper.insertOrUpdate(customerId);
        if (rows == 0) {
            log.error("无法创建客户IM账号登陆记录，客户账号ID：" + customerId);
            throw new HisException("客服系统异常");
        }
        //TODO 发送欢迎语
        sendWelcomeMessage(account);

        return result;
    }

    @Override
    public Map<String, Object> getServiceAccount() {
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        //生成客户账号签名
        String userSig = api.genUserSig(customerServiceId, 180 * 86400);
        //保存返回的结果
        Map<String, Object> result = new HashMap();
        result.put("sdkAppId", sdkAppId);
        result.put("account", customerServiceId);
        result.put("userSig", userSig);

        return result;
    }

    private void sendWelcomeMessage(String account) {
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        //生成客服账号签名
        String userSig = api.genUserSig(customerServiceId, 180 * 86400);
        String url = baseUrl + "v4/openim/sendmsg?sdkappid=" + sdkAppId + "&identifier=" + customerServiceId + "&usersig=" + userSig + "&random=" + RandomUtil.randomInt(1, 99999999) + "&contenttype=json";
        JSONObject json = new JSONObject();
        json.set("SyncOtherMachine", 2); //欢迎词消息不同步至发送方
        json.set("To_Account", account);
        json.set("MsgLifeTime", 120); //消息保存两分钟
        json.set("MsgRandom", RandomUtil.randomInt(1, 99999999)); //用于消息去重
        json.set("MsgBody", new ArrayList<>() {{
            add(new HashMap<>() {{
                put("MsgType", "TIMTextElem"); //文本消息
                put("MsgContent", new HashMap<>() {{
                    put("Text", "亲，您好，非常高兴为您服务，有什么可以为您效劳的呢?");
                }});
            }});
        }});
        String response = HttpUtil.post(url, json.toString());
        JSONObject entries = JSONUtil.parseObj(response);
        int errorCode = entries.getInt("ErrorCode");
        String errorInfo = entries.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("发送欢迎词失败：" + errorInfo.toString());
            throw new HisException("客服系统异常");
        }
    }
}