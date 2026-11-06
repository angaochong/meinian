package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.SysRoleEntity;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【sys_role(系统角色表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.SysRoleEntity
*/
public interface SysRoleMapper {

    List<Map<String, Object>> selectAll();

}




