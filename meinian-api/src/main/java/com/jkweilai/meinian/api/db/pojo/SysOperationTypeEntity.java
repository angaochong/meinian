package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName sys_operation_type
 */
@Data
public class SysOperationTypeEntity {
    private Integer id;

    private String operationCode;

    private String operationName;
}