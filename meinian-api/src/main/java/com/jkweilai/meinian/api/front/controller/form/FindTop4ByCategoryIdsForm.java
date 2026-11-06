package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FindTop4ByCategoryIdsForm {
    @NotEmpty(message = "categoryIds不能为空")
    private Integer[] categoryIds;
}
