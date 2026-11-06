package com.jkweilai.meinian.api.front.service;

import java.util.Map;

public interface CrmCustomerService {

    //发送短信
    boolean sendSmsCode(String phone);

    //登录
    Map<String, Object> login(String phone, String code);

    //查询顾客信息和订单信息的摘要
    Map<String, Object> getSummaryById(Integer id);

    //修改个人信息
    boolean modify(Map<String, Object> param);

    //查看顾客电话号码是否已存在
    boolean phoneExist(String phone, Integer id);
}
