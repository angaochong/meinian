package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveRuleByIdForm {
    @NotNull(message = "ruleId不能为空")
    @Min(value = 1, message = "ruleId不能小于1")
    private Integer ruleId;
}
