package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ModifyRuleForm {
    @NotNull(message = "ruleId不能为空")
    @Min(value = 1, message = "ruleId不能小于1")
    private Integer ruleId;

    @NotBlank(message = "ruleName不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{1,20}$", message = "ruleName内容不正确")
    private String ruleName;

    @NotBlank(message = "ruleContent不能为空")
    private String ruleContent;

    private String remark;
}
