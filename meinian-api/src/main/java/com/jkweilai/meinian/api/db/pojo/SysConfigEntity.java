package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;

/**
 * @TableName sys_config
 */
@Data
public class SysConfigEntity {
    private Integer configId;

    private String configKey;

    private String configValue;

    private String remark;
}