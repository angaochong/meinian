package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.MedExamReportEntity;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【med_exam_report(体检报告表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.MedExamReportEntity
*/
public interface MedExamReportMapper {

    //新增体检报告
    int insert(Map<String, Object> param);

    //分页查询体检报告数据
    List<Map<String, Object>> selectPage(Map<String, Object> param);

    //分页查询体检报告记录条数
    long selectCount(Map<String, Object> param);

    //查询体检报告记录
    Map<String, Object> selectById(Integer id);

    //更新体检报告状态、在minio上的地址、快递编号、邮寄日期
    int update(Map<String, Object> param);

    //查询50个可以生成体检报告的记录
    List<Integer> select50NonReport();

}




