package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.pojo.PromotionRuleEntity;

import java.util.List;
import java.util.Map;

public interface PromotionRuleService {

    //查询促销规则的id名字
    List<Map<String, Object>> findAll();

    //分页查询促销规则表
    PageResult<Map<String,Object>> pageQueryByCondition(Map<String, Object> param);

    //新增促销规则
    int save(PromotionRuleEntity promotionRuleEntity);

    //根据规则id查询规则
    PromotionRuleEntity findById(int id);

    //修改规则
    int modifyById(PromotionRuleEntity promotionRuleEntity);

    //删除促销规则
    int removeById(Integer ruleId);
}
