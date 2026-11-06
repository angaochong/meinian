package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SaveAppointmentForm {
    @NotNull(message = "orderId不能为空")
    @Min(value = 1, message = "orderId不能小于1")
    private Integer orderId;

    @NotBlank(message = "appointmentDate不能为空")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "date内容不正确")
    private String appointmentDate;

    @NotBlank(message = "patientName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "patientName内容不正确")
    private String patientName;

    @NotBlank(message = "idCardNo不能为空")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "idCardNo内容不正确")
    private String idCardNo;

    @NotBlank(message = "phone不能为空")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "phone内容不正确")
    private String phone;

    @NotBlank(message = "address不能为空")
    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5\\-_#]{10,100}", message = "address内容不正确")
    private String address;

    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5\\-_#]{2,100}", message = "company内容不正确")
    private String company;
}
