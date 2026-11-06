package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.PromotionRuleMapper;
import com.jkweilai.meinian.api.db.pojo.PromotionRuleEntity;
import com.jkweilai.meinian.api.mis.service.PromotionRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PromotionRuleServiceImpl implements PromotionRuleService {

    @Resource
    private PromotionRuleMapper promotionRuleMapper;

    @Override
    public List<Map<String, Object>> findAll() {
        return promotionRuleMapper.selectAll();
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        List<Map<String, Object>> records = new ArrayList<>();
        long count = promotionRuleMapper.selectCountByQueryCondition(param);
        if(count > 0) {
            records = promotionRuleMapper.selectPageByQueryCondition(param);
        }
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        return new PageResult<>(count, pageNo, pageSize, records);
    }

    @Override
    @Transactional
    public int save(PromotionRuleEntity promotionRuleEntity) {
        return promotionRuleMapper.insert(promotionRuleEntity);
    }

    @Override
    public PromotionRuleEntity findById(int id) {
        return promotionRuleMapper.selectById(id);
    }

    @Override
    @Transactional
    public int modifyById(PromotionRuleEntity promotionRuleEntity) {
        return promotionRuleMapper.updateById(promotionRuleEntity);
    }

    @Override
    @Transactional
    public int removeById(Integer ruleId) {
        return  promotionRuleMapper.deleteById(ruleId);
    }
}
