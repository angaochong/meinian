package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName sys_department
 */
@Data
public class SysDepartmentEntity {
    private Integer id;

    private String deptName;

    private String telephone;

    private String email;

    private String remark;
}