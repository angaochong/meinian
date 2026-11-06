package com.jkweilai.meinian.api.front.service;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.pojo.MedExamAppointmentEntity;

import java.util.Map;

public interface AppointmentService {

    //保存预约信息
    String save(MedExamAppointmentEntity entity);

    //分页查询预约记录
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);
}
