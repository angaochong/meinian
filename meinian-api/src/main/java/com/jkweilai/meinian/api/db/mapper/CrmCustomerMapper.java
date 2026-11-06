package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.CrmCustomerEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author admin
* @description 针对表【crm_customer(客户信息表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.CrmCustomerEntity
*/
public interface CrmCustomerMapper {

    //保存客户信息
    int insert(CrmCustomerEntity entity);

    //根据手机号获取客户id
    Integer selectByPhone(String phone);

    //查询顾客信息
    Map<String, Object> selectById(Integer id);

    //修改个人资料
    int update(Map<String, Object> param);

    //查看电话号码是否已存在
    int countPhoneExist(@Param("phone") String phone, @Param("id") Integer id);

}




