package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FindAppointmentByAppointmentNoForm {
    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;
}