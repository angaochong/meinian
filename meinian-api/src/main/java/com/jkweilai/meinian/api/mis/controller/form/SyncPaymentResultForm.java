package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SyncPaymentResultForm {
    @NotEmpty(message = "outTradeNos不能为空")
    private String[] outTradeNos;
}