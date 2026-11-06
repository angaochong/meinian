package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HasAppointmentInTodayForm {
    @NotBlank(message = "patientName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "patientName内容不正确")
    private String patientName;

    @NotBlank(message = "gender不能为空")
    @Pattern(regexp = "^男$|^女$", message = "gender内容不正确")
    private String gender;

    @NotBlank(message = "idCardNo不能为空")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "身份证号码无效")
    private String idCardNo;
}
