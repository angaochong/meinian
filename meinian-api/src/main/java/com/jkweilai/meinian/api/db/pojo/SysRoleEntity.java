package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName sys_role
 */
@Data
public class SysRoleEntity {
    private Integer roleId;

    private String roleName;

    private String roleDesc;

    private String permissionIds;

    private String defaultPermissions;

    private String isSystem;
}