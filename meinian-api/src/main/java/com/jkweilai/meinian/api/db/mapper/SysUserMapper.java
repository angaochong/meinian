package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.SysUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author admin
* @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.SysUserEntity
*/
public interface SysUserMapper {
    Set<String> getPermissionsByUserId(int userId);

    //@param里面是给参数起别名，将来xml文件#{username}取值
    //返回userId
    Integer selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    //查询用户名
    String selectUserNameById(int id);

    //更新密码
    int updatePassword(@Param("userId") int id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);

    //根据条件分页查询`
    List<Map<String, Object>> selectPageByQueryCondition(Map<String, Object> params);

    //查询的总记录条数
    long selectCountByQueryCondition(Map<String, Object> params);

    //新增用户
    int insert(SysUserEntity user);

    //查询用户信息
    Map<String, Object> selectById(int userId);

    //更新模态窗口的用户信息
    int update(Map<String, Object> user);

    //删除用户
    int deleteByIds(Integer[] ids);

    //离职
    int updateStatus(int userId);

    //医生信息
    Map<String, Object> selectDoctorById(int id);
    
}




