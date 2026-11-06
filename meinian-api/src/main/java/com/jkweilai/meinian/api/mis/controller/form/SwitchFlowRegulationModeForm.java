package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchFlowRegulationModeForm {
    @NotNull(message = "mode不能为空")
    private Boolean mode;
}
