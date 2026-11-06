package com.jkweilai.meinian.api.socket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/websocket/push/message")
@Component
public class MessagePushEndpoint {

    /*
        key存储的是用户的id，value是这个用户对应的Socket连接。
        假设业务端的id=1的用户访问，则key是 customer_1
        假设mis端的id=1的用户访问，则key是 user_1
        每一个用户都有自己专属的Socket连接（也就是Session对象）
        并且Socket连接永不共享，一个用户对应一个。
        customer_1==>对应自己的Socket连接。
        user_1==>对应自己的Socket连接。
        一个Socket连接不能让张三用了之后，李四又用，这是不可能的。
     */
    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    // Socket连接被创建时自动调用。
    @OnOpen
    public void onOpen(Session session) {
        // 设置会话超时时间，防止长时间空闲连接
        session.setMaxIdleTimeout(30 * 60 * 1000); // 30分钟
    }

    // 客户端每一次向服务器发送消息时的回调
    // 该方法的作用是：将用户的id和对应的socket绑定到sessionMap中。（注册）
    /*
        假如客户端通过websocket发消息的时候，采用了以下格式：
            ws.send(JSON.stringify({
                "opt": "register",      // 操作类型（代码有ping判断）
                "identity": "customer", // 用户身份
                "token": "eyJhbGciOiJ..." // 认证令牌
            }));
    */
    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject json = JSONUtil.parseObj(message);
        String opt = json.getStr("opt");

        if ("ping".equals(opt)) {
            return;
        }
        // 获取用户的身份
        String identity = json.getStr("identity");
        // 获取令牌
        String token = json.getStr("token");
        // 生成key
        String userId = null;
        if ("customer".equals(identity)) {
            userId = "customer_" + StpCustomerUtil.getLoginIdByToken(token).toString();
        } else {
            userId = "user_" + StpUtil.getLoginIdByToken(token).toString();
        }

        // 这是 WebSocket Session 的标准方法，getUserProperties() 返回一个 Map
        // 用于在 Session 的整个生命周期中存储自定义数据
        Map<String, Object> map = session.getUserProperties();
        map.put("userId", userId);

        // 直接put，ConcurrentHashMap是线程安全的
        // 不会创建新的Socket连接，只是sessionMap中的value会被覆盖
        sessionMap.put(userId, session);
    }

    // Socket连接被关闭时自动调用。
    @OnClose
    public void onClose(Session session) {
        // 这个代码的逻辑是：关闭Socket连接时，将 sessionMap 中对应的键值对移除。
        Map map = session.getUserProperties();
        if (map.containsKey("userId")) {
            String userId = MapUtil.getStr(map, "userId");
            // 确保只移除当前会话，避免移除其他新会话
            Session currentSession = sessionMap.get(userId);
            if (currentSession == session) {
                sessionMap.remove(userId);
            }
        }
    }

    // 发生错误时的回调
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
        // 错误时也清理连接
        onClose(session);
    }

    // 服务器向客户端发消息的方法。
    public static void sendInfo(String message, String userId) {
        if (StrUtil.isBlank(userId)) return;

        Session session = sessionMap.get(userId);
        if (session == null || !session.isOpen()) {
            sessionMap.remove(userId);
            return;
        }

        try {
            // 服务端向客户端发送消息的核心代码
            // session：代表一个用户的WebSocket连接
            // getBasicRemote()：获取消息发送接口
            // sendText(message)：发送文本消息到客户端
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("发送消息异常, userId: {}", userId, e);
            sessionMap.remove(userId);
        }
    }


}