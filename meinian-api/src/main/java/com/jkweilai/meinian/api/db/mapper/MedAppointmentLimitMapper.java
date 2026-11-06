package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.MedAppointmentLimitEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【med_appointment_limit(体检预约限流配置表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.MedAppointmentLimitEntity
*/
public interface MedAppointmentLimitMapper {

    //查看预约限流表中是否有当天的限流规则
    List<Map<String, Object>> selectAppointmentLimitLast60Days(Map<String, Object> param);

    //定时任务，查看现流表中是否有要更新的那一天的限流规则
    Map<String, Object> selectByAppointmentDate(@Param("appointmentDate") String date);

    //新增或修改预约限流表
    int saveOrUpdateAppointmentLimit(Map<String, Object> param);

}




