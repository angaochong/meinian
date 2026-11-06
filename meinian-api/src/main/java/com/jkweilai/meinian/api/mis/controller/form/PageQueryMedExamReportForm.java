package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class PageQueryMedExamReportForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "patientName内容不正确")
    private String patientName;

    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "phone内容不正确")
    private String phone;

    @Pattern(regexp = "^[0-9A-Za-z]{10,24}$", message = "expressNo内容不正确")
    private String expressNo;

    @Range(min = 1, max = 3, message = "status内容不正确")
    private Integer status;

    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}
