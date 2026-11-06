package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FindCheckupByAppointmentNoAndPlaceForm {
    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;

    @NotBlank(message = "place不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{2,30}$", message = "place内容不正确")
    private String place;
}
