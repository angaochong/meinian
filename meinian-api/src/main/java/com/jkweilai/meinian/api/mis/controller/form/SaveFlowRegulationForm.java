package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class SaveFlowRegulationForm {
    @NotBlank(message = "departmentName不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,40}$", message = "departmentName内容不正确")
    private String departmentName;

    @NotBlank(message = "beaconUuid不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{32}$", message = "beaconUuid内容不正确")
    private String beaconUuid;

    @NotNull(message = "maxCapacity不能为空")
    @Range(min = 1, max = 1000, message = "maxCapacity内容不正确")
    private Integer maxCapacity;

    @NotNull(message = "weight不能为空")
    @Range(min = 1, max = 10, message = "weight内容不正确")
    private Integer weight;

    @NotNull(message = "priority不能为空")
    @Range(min = 1, max = 10, message = "priority内容不正确")
    private Integer priority;
}
