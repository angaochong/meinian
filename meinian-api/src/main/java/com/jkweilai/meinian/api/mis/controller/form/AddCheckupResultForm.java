package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class AddCheckupResultForm {
    @NotBlank(message = "doctorName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "doctorName内容不正确")
    private String doctorName;

    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;

    @NotBlank(message = "place不能为空")
    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5]{2,30}$", message = "place内容不正确")
    private String place;

    @NotEmpty(message = "item不能为空")
    private List item;

    @NotEmpty(message = "template不能为空")
    private String template;
}
