package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentForm {

    @NotNull(message = "goodsId不能为空")
    @Min(value = 1, message = "goodsId不能小于1")
    private Integer goodsId;

    @NotNull(message = "buyCount不能为空")
    @Min(value = 1, message = "buyCount不能小于1")
    private Integer buyCount;
}
