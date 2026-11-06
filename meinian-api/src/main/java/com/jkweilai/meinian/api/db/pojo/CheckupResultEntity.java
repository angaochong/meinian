package com.jkweilai.meinian.api.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * 体检结果实体类
 * 对应MongoDB中的 checkup_result 集合
 */
@Data
@Document(collection = "checkup_result")  // 指定MongoDB集合名称为 checkup_result
public class CheckupResultEntity {
    /**
     * MongoDB主键ID
     * 使用MongoDB自动生成的ObjectId作为主键
     */
    @Id
    private String _id;

    /**
     * 唯一标识符（建立索引）
     * 用于关联体检单号或体检人信息
     */
    @Indexed  // 为该字段创建索引，提高查询性能
    private String uuid;

    /**
     * 体检项目列表
     */
    private List<Map> checkup;

    /**
     * 体检地点列表
     * 记录体检过程中涉及的不同科室或地点
     * 示例：["内科", "外科", "眼科"]
     */
    private List<String> place;

    /**
     * 体检结果列表
     * 存储最终的体检结论或汇总结果
     * 示例：[{"conclusion": "健康状况良好", "suggestion": "注意休息"}]
     */
    private List<Map> result;
}