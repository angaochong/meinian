package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RemoveUserByIdsForm {
    @NotEmpty(message = "userIds不能为空")
    private Integer[] userIds;
}