package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FindGoodsBySnapshotIdForm {
    @NotBlank(message = "snapshotId不能为空")
    @Pattern(regexp = "^[0-9a-z]{24}$", message = "snapshotId内容不正确")
    private String snapshotId;
}
