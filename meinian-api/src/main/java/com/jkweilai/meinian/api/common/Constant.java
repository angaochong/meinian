package com.jkweilai.meinian.api.common;

public class Constant {

    // 限流规则的redis的key的前缀。
    public static final String APPOINTMENT_LIMIT_PREFIX = "appointment#";

    // 是否启动自动调流规则的配置的前缀。
    public static final String AUTO_FLOW_REGULATION_PREFIX = "setting#";

    // 预约已满的提示信息。
    public static final String APPOINTMENT_FULL = "今日预约已满，请选择其他日期";

    // 预约成功
    public static final String APPOINTMENT_SUCCESS = "预约成功";

    // 预约失败
    public static final String APPOINTMENT_FAIL = "预约失败";
}
