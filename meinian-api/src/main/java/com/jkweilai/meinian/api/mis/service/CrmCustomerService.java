package com.jkweilai.meinian.api.mis.service;

import java.util.Map;

public interface CrmCustomerService {

    //查找顾客摘要信息
    Map<String, Object> findSummary(Integer customerId);
}
