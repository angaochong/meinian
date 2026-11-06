package com.jkweilai.meinian.api.mis.service;

public interface SysConfigService {

    //获取自动调流开关的值
    String getItemValue(String key);

    //修改自动调流的值
    boolean setConfigValue(String configKey, String configValue);
}
