package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ModifyStatusByAppointmentNoForm {
    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;

    @NotNull(message = "status不能为空")
    @Range(min = 1, max = 4, message = "status内容不正确")
    private Integer status;
}