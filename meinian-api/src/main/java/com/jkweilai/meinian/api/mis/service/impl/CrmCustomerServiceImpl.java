package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.db.mapper.CrmCustomerMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.mis.service.CrmCustomerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CrmCustomerServiceImpl implements CrmCustomerService {

    @Resource
    private CrmCustomerMapper crmCustomerMapper;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Override
    public Map<String, Object> findSummary(Integer customerId) {
        Map<String, Object> map = crmCustomerMapper.selectById(customerId);
        Map<String, Object> map1 = tradeOrderMapper.selectOrderStatistics(customerId);
        map.putAll(map1);
        return map;
    }
}
