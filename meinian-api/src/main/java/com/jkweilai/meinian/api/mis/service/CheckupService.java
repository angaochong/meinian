package com.jkweilai.meinian.api.mis.service;

import java.util.Map;

public interface CheckupService {

    // 查询体检人在当前科室对应的体检项目
    Map findCheckupByPlace(String uuid, String place);

    //保存体检结果
    void addResult(String uuid, String place, Map map);
}
