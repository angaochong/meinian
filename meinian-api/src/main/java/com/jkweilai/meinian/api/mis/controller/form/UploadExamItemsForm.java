package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadExamItemsForm {
    @NotNull(message = "id不能小于1")
    @Min(value = 1, message = "id不能小于1")
    private Integer id;
}
