package com.jkweilai.meinian.api.front.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginForm {
    @NotBlank(message = "phone不能为空")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "phone内容错误")
    private String phone;

    @NotBlank(message = "code不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "code内容错误")
    private String code;
}

