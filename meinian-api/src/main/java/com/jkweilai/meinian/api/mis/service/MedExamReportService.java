package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;

import java.util.Map;

public interface MedExamReportService {

    //分页查询体检报告
    PageResult<Map<String, Object>> pageQuery(Map<String, Object> param);

    //生成体检报告的核心接口
    boolean createReport(Integer id);

    //识别快递运单接口
    Map<String, Object> recognizeWaybill(String imageBase64);

    //上传运单成功后更新数据库
    boolean saveWaybill(Map<String, Object> param);
}
