package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.pojo.MedDepartmentFlowEntity;

import java.util.List;
import java.util.Map;

public interface FlowRegulationService {

    //查询科室名称
    List<String> findDepartmentNames();

    //分页查询体检调流页面
    PageResult<MedDepartmentFlowEntity> pageQuery(Map<String, Object> params);

    //添加科室调流记录
    int save(MedDepartmentFlowEntity entity);

    //加载修改弹窗的信息
    MedDepartmentFlowEntity findById(int id);

    //修改调流记录（新增也是这个个函数）
    int modify(Map<String, Object> params);

    //删除科室调流记录
    int removeByIds(Integer[] ids);
}
