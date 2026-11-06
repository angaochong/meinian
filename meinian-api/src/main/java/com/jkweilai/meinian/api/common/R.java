package com.jkweilai.meinian.api.common;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果封装类
 * 继承自HashMap，提供链式操作，用于规范所有Controller方法的返回格式
 */
public class R extends HashMap<String, Object> {

    /**
     * 默认构造器：初始化一个成功的响应
     * 设计意图：默认创建即表示成功，符合大多数成功场景，减少冗余设置。
     */
    public R() {
        // 默认创建的R对象中包含了公共的属性
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }

    /**
     * 重写put方法，支持链式调用
     * 例如：r.put("data", data).put("token", token);
     * 设计意图：提升代码的流畅度和可读性，便于动态添加多个自定义字段。
     *
     * @param key   键
     * @param value 值
     * @return 当前R对象自身
     */
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        // 返回自己，用于链式调用
        return this;
    }

    /**
     * 创建一个表示【成功】的响应对象（无附加数据）
     * 设计意图：用于操作成功但无需返回业务数据的场景，如新增、删除、修改操作。
     *
     * @return 默认的成功响应对象
     */
    public static R ok() {
        return new R();
    }

    /**
     * 创建一个表示【成功】的响应对象，并自定义成功消息
     * 设计意图：在成功的基础上，提供更具体的成功提示信息。
     *
     * @param msg 自定义的成功消息
     * @return 带有自定义消息的成功响应对象
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 创建一个表示【成功】的响应对象，并批量添加数据
     * 设计意图：当需要返回多个键值对数据时，可以传入一个Map，避免多次调用put方法。
     *
     * @param map 包含多个键值对数据的Map
     * @return 包含了指定Map所有数据的成功响应对象
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 创建一个表示【错误】的响应对象，并指定错误码和错误信息
     * 设计意图：提供最灵活的错误创建方式，允许自定义业务错误码和错误信息。
     *
     * @param code 错误码（通常使用HTTP状态码或自定义业务状态码）
     * @param msg  错误信息
     * @return 错误响应对象
     */
    public static R error(int code, String msg) {
        R r = new R();
        // 覆盖默认的成功状态码和消息
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 创建一个表示【错误】的响应对象，使用默认错误码（500）
     * 设计意图：简化错误创建，在仅需提供错误信息而错误码可默认为500时使用。
     *
     * @param msg 错误信息
     * @return 错误响应对象
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 创建一个表示【错误】的响应对象，使用默认错误码和错误信息
     * 设计意图：作为最后的兜底方案，在捕获到未知异常时返回，避免暴露系统内部细节。
     *
     * @return 使用默认错误码和信息的错误响应对象
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }
}