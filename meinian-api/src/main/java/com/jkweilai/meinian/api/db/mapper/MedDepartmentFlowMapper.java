package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.MedDepartmentFlowEntity;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【med_department_flow(科室流量调控表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.MedDepartmentFlowEntity
*/
public interface MedDepartmentFlowMapper {

    //查询科室名称
    List<String> selectDepartmentNames();

    //分页查询体检调流页面数据
    List<MedDepartmentFlowEntity> selectPage(Map<String, Object> params);

    //分页查询体检调流页面记录条数
    long selectCount(Map<String, Object> params);

    //添加科室调流记录
    int insert(MedDepartmentFlowEntity entity);

    //加载修改弹窗信息
    MedDepartmentFlowEntity selectById(int id);

    //修改调流记录（新增也是这个个函数）
    int update(Map<String, Object> param);

    // 更新科室门口当前人数（这是一个通用的方法，可以一次将每个科室的当前人数统一更新为0，也可以将某个科室的当前人数更新为指定值）
    int updateCurrentCount(Map<String, Object> param);

    // 根据权重获取推荐列表
    List<Map<String,Object>> selectRecommendationsWithWeight();

    // 根据优先级获取推荐列表
    List<Map<String,Object>> selectRecommendationsWithPriority();

    //查询所有科室 id 和 名字
    List<Map<String, Object>> selectAllDept();

    //删除科室调流记录
    int deleteByIds(Integer[] ids);

}




