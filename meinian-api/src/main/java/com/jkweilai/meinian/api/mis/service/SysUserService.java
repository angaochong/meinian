package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.pojo.SysUserEntity;

import java.util.Map;

public interface SysUserService {

    //登录
    Integer login(Map<String, Object> userMap);

    //修改密码
    boolean modifyPassword(Map<String, Object> param);

    //根据条件分页查询
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //新增用户
    int save(SysUserEntity user);

    //查询用户信息
    Map<String, Object> findById(int userId);

    //更新模态窗口的用户信息
    int modify(Map<String, Object> user);

    //删除用户
    int removeByIds(Integer[] ids);

    //离职
    int dismiss(int userId);

    //查找医生信息
    Map<String, Object> findDoctorById(int id);
}
