package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.SysConfigEntity;
import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【sys_config(系统配置表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.SysConfigEntity
*/
public interface SysConfigMapper {

    //查询系统配置列表
    List<SysConfigEntity> selectAll();

    //修改自动调流的值
    int updateByConfigKey(Map<String, String> params);
}




