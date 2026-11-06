package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.PromotionRuleEntity;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【promotion_rule(促销规则表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.PromotionRuleEntity
*/
public interface PromotionRuleMapper {

    //查询促销规则的id名字
    List<Map<String, Object>> selectAll();

    //分页查询促销规则表数据
    List<Map<String, Object>> selectPageByQueryCondition(Map<String, Object> params);

    //分页查询促销规则数量
    long selectCountByQueryCondition(Map<String, Object> params);

    //新增规则
    int insert(PromotionRuleEntity promotionRuleEntity);

    //根据规则id查询规则
    PromotionRuleEntity selectById(Integer ruleId);

    //修改规则
    int updateById(PromotionRuleEntity promotionRuleEntity);

    //删除促销规则
    int deleteById(Integer ruleId);
}




