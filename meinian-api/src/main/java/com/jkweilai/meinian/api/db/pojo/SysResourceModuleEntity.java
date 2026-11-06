package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName sys_resource_module
 */
@Data
public class SysResourceModuleEntity {
    private Integer moduleId;

    private String moduleCode;

    private String moduleName;
}