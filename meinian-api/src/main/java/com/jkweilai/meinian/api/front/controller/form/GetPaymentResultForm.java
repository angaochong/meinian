package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GetPaymentResultForm {
    @NotBlank(message = "outTradeNo不能为空")
    @Pattern(regexp = "^[0-9A-Z]{32}$", message = "outTradeNo内容不正确")
    private String outTradeNo;
}
