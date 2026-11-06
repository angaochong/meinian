package com.jkweilai.meinian.api.front.service;

import java.util.Map;

public interface CustomerImService {

    //创建客户IM账号
    Map<String, Object> createAccount(int customerId);

    //创建客服IM账号
    Map<String, Object> getServiceAccount();
}