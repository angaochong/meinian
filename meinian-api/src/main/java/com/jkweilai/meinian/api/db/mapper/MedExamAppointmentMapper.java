package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.MedExamAppointmentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【med_exam_appointment(体检预约记录表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.MedExamAppointmentEntity
*/
public interface MedExamAppointmentMapper {

    //根据订单id查询预约信息
    List<Map<String, Object>> selectByOrderId(Integer orderId);

    //新增预约表数据
    int insert(MedExamAppointmentEntity entity);

    //front分页查询体检预约记录
    List<Map<String, Object>> selectPageByQueryCondition(Map<String, Object> params);

    //front分页查询体检预约记录条数
    long selectCountByQueryCondition(Map<String, Object> params);

    //mis分页查询体检预约记录
    List<Map<String, Object>> selectPage(Map<String, Object> param);

    //mis分页查询体检预约记录条数
    long selectCount(Map<String, Object> param);

    //mis删除未签到的体检预约记录
    int deleteByIds(Integer[] ids);

    //查看用户预约的情况（0：没有预约，-1：有预约，但已经办理了签到，1：有预约，但还未办理签到）
    Map<String, Object> selectAppointmentInToday(Map<String, Object> param);

    //客户签到后，更新订单状态
    int updateForCheckin(Map<String, Object> param);

    //查询预约编号和商品快照id
    Map<String, Object> selectAppointNoAndSnapshotId(Map<String, Object> param);

    //导引单 的摘要信息
    Map<String, Object> selectSummaryById(Integer id);

    //完成体检后，更新预约记录状态
    int updateStatusByAppointmentNo(Map<String, Object> param);

    //查询体检人信息
    Map<String, Object> selectByAppointmentNo(String appointmentNo);

    //获取体检人信息、套餐名、预约编号
    Map<String, Object> selectDataForReport(Integer id);

}




