package com.jkweilai.meinian.api.db.dao;

import com.jkweilai.meinian.api.db.pojo.GoodsSnapshotEntity;
import jakarta.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GoodsSnapshotDao {

    @Resource
    private MongoTemplate mongoTemplate;

    // 根据md5Hash判断是否存在商品快照，存在就返回快照 id，不存在则返回null
    public String hasGoodsSnapshot(String md5Hash) {
        // 构建查询条件：根据md5Hash字段精确匹配
        Criteria criteria = Criteria.where("md5Hash").is(md5Hash);
        Query query = new Query(criteria);

        // 设置分页参数：只查询第一条匹配的记录，提高查询效率
        query.skip(0);  // 从第0条开始
        query.limit(1); // 只取1条记录（拿到1个就不再继续扫描了，效率高。）

        // 执行查询，返回单个实体对象
        GoodsSnapshotEntity entity = mongoTemplate.findOne(query, GoodsSnapshotEntity.class);

        // 如果找到记录则返回其主键_id，否则返回null
        return entity != null ? entity.get_id() : null;
    }

    // 保存或更新快照信息。
    public String insert(GoodsSnapshotEntity entity) {
        // 使用save方法：当entity的_id为null时执行插入，否则执行更新
        // 返回保存后的实体，并获取其主键_id
        return mongoTemplate.save(entity).get_id();
    }

    //根据商品快照id获取商品快照信息
    public GoodsSnapshotEntity findById(String snapshotId) {
        return mongoTemplate.findById(snapshotId,  GoodsSnapshotEntity.class);
    }

    /**
     * 查询适合指定性别的体检项目列表
     * @param id 体检项目快照记录的ID
     * @param sex 体检人的性别（"男" 或 "女"）
     * @return 适合该性别的体检项目列表
     */
    public List<Map> searchCheckup(String id, String sex) {
        // 创建MongoDB聚合管道，用于复杂的数据处理和筛选
        Aggregation aggregation = Aggregation.newAggregation(
                // 第一阶段：投影 - 只保留examItems字段，其他字段不返回
                // 注意：主键 _id 会自动投影，不用写。
                Aggregation.project("examItems"),

                // 第二阶段：展开 - 将examItems数组拆分成多个独立的文档
                // 例如：原文档{examItems: [项目1, 项目2, 项目3]}
                // 展开后变成：[{examItems: 项目1}, {examItems: 项目2}, {examItems: 项目3}]
                Aggregation.unwind("$examItems"),

                // 第三阶段：匹配 - 筛选符合条件的文档
                Aggregation.match(
                        Criteria.where("_id").is(id) // 根据ID找到指定的体检项目快照记录
                                // 筛选体检项目：只保留性别为"无"或与传入性别匹配的项目
                                // 比如sex="男"，就保留项目中sex="无"或sex="男"的项目，排除sex="女"的项目
                                .and("examItems.sex").in("无", sex)
                )
        );

        // 执行聚合查询
        // 参数说明：聚合管道, 集合名称, 返回结果类型
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "goods_snapshot", HashMap.class);

        // 处理查询结果，提取需要的体检项目数据
        List<Map> list = new ArrayList<>();
        results.getMappedResults().forEach(one -> {
            // 从每个结果文档中提取examItems字段，这里面就是具体的体检项目信息
            HashMap map = (HashMap) one.get("examItems");
            list.add(map);
        });

        return list;
    }
}
