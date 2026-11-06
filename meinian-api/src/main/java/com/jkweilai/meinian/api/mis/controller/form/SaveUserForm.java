package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveUserForm {

    @NotBlank(message = "username不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "username内容不正确")
    private String username;

    @NotBlank(message = "password不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "password内容不正确")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "姓名内容不正确")
    private String realName;

    @NotBlank(message = "gender不能为空")
    @Pattern(regexp = "^男$|^女$", message = "gender内容不正确")
    private String gender;

    @NotBlank(message = "mobile不能为空")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "mobile内容不正确")
    private String mobile;

    @NotBlank(message = "email内容不正确")
    @Email(message = "email内容不正确")
    private String email;

    @Min(value = 1, message = "deptId不能小于1")
    private Integer deptId;

    @NotBlank(message = "hireDate不能为空")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "hireDate内容不正确")
    private String hireDate;

    @NotEmpty(message = "roleIds不能为空")
    private Integer[] roleIds;
}