package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class FlowRegulationPageQueryForm {
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5\\(\\)]{1,40}$", message = "deptName内容不正确")
    private String deptName;

    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "beaconUuid内容不正确")
    private String beaconUuid;

    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}
