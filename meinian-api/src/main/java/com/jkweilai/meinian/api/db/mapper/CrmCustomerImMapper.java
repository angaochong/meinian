package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.CrmCustomerImEntity;

/**
* @author admin
* @description 针对表【crm_customer_im(客户IM账号表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.CrmCustomerImEntity
*/
public interface CrmCustomerImMapper {

    //如果是第一次创建则会新增记录，否则就更新
    int insertOrUpdate(Integer customerId);

}




