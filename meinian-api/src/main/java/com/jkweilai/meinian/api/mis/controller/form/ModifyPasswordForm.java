package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ModifyPasswordForm {

    //前端传给后端 JSON 里的key 字段名，必须和这个 Form 类的成员变量名完全一致
    @NotBlank(message = "原密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "原密码格式错误")
    String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "新密码格式错误")
    private String newPassword;
}
