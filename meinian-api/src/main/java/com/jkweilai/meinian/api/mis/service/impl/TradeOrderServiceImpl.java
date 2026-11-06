package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.front.service.PaymentService;
import com.jkweilai.meinian.api.mis.service.TradeOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeOrderServiceImpl implements TradeOrderService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private PaymentService  paymentService;

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        long total = tradeOrderMapper.selectCount(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = tradeOrderMapper.selectPage(param);
        }
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        Integer pageNo = MapUtil.getInt(param, "pageNo");

        return new PageResult<Map<String, Object>>(total, pageSize, pageNo, records);
    }

    @Override
    @Transactional
    public int syncPaymentResult(String[] outTradeNos) {
        if(outTradeNos ==null || outTradeNos.length==0){
            return 0;
        }
        int rows = 0;
        for (String outTradeNo : outTradeNos) {
            String transactionId = paymentService.getPaymentResult(outTradeNo);
            if(transactionId != null){
                HashMap<String, Object> param = new HashMap<>();
                param.put("outTradeNo", outTradeNo);
                param.put("transactionId", transactionId);
                rows += tradeOrderMapper.updatePayment(param);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int removeById(Integer id) {
        return tradeOrderMapper.deleteById(id);
    }

    @Override
    public boolean processOfflineRefund(String outTradeNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("outTradeNo", outTradeNo);
        String outRefundNo = IdUtil.simpleUUID();
        param.put("outRefundNo", outRefundNo);
        return tradeOrderMapper.updateStatusOfflineRefundByOutTradeNo(param) == 1;
    }
}
