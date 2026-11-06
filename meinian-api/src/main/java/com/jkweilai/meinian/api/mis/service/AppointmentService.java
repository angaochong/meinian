package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;

import java.util.List;
import java.util.Map;

public interface AppointmentService {

    //根据订单id查询预约信息
    List<Map<String, Object>> findByOrderId(Integer orderId);

    //分页查询预约记录
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //删除预约记录
    int removeByIds(Integer[] ids);

    //查看用户的预约情况
    int hasAppointmentInToday(Map<String, Object> param);

    //签到核心方法
    boolean checkin(Map<String, Object> param);

    //导引单 摘要
    Map<String, Object> findGuidanceInfo(Integer id);

    //完成体检，修改状态
    boolean modifyStatusByAppointmentNo(Map<String, Object> param);

    //查询体检人信息
    Map<String, Object> findByAppointmentNo(String appointmentNo);
}
