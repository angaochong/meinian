package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RemoveMedDepartmentFlowByIdsForm {
    @NotEmpty(message = "ids不能为空")
    private Integer[] ids;
}
