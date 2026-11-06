package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessOfflineRefundForm {

    @NotBlank(message = "订单流水号不能为空")
    @Size(min = 16, max = 32, message = "订单流水号长度应在16-32位之间")
    @Pattern(regexp = "^[A-F0-9]+$", message = "订单流水号必须是十六进制字符（0-9, A-F）")
    private String outTradeNo;
}