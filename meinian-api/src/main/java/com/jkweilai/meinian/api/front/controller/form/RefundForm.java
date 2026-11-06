package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundForm {

    @NotNull
    @Min(value = 1, message = "orderId不能小于1")
    private Integer orderId;
}