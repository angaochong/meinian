package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class UserPageQueryForm {
    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须在10~50之间")
    private Integer pageSize;

    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "realName内容不正确")
    private String realName;

    @Pattern(regexp = "^男$|^女$", message = "gender内容不正确")
    private String gender;

    @Min(value = 0, message = "roleId不能小于0")
    private Integer roleId;

    @Min(value = 1, message = "deptId不能小于1")
    private Integer deptId;

    @Range(min = 1, max = 2, message = "userStatus只能是1或2")
    private Integer userStatus;

}