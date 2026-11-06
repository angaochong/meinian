package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName promotion_rule
 */
@Data
public class PromotionRuleEntity {
    private Integer ruleId;

    private String ruleName;

    private String ruleContent;

    private String remark;
}