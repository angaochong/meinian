package com.jkweilai.meinian.api.db.dao;

import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.db.pojo.CheckupResultEntity;
import com.mongodb.client.MongoClient;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * 体检结果数据访问层
 * 负责体检结果数据的数据库操作
 */
@Repository  // 标识为数据访问层组件，由Spring管理
public class CheckupResultDao {

    @Resource  // 注入MongoDB操作模板
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoClient mongo;

    /**
     * 插入体检结果记录
     *
     * @param uuid    体检记录唯一标识符，用于关联体检单或体检人
     * @param checkup 体检项目列表，包含具体的检查项目和数据
     * @return boolean 插入是否成功，true-成功，false-失败
     */
    public boolean insert(String uuid, List<Map> checkup) {
        // 创建体检结果实体对象
        CheckupResultEntity entity = new CheckupResultEntity();

        // 设置实体属性
        entity.setUuid(uuid);        // 设置唯一标识
        entity.setCheckup(checkup);  // 设置体检项目数据

        // 初始化空的地点列表
        entity.setPlace(new ArrayList<>());

        // 初始化空的结果列表
        entity.setResult(new ArrayList<>());

        // 执行数据库插入操作
        // mongoTemplate.insert()方法会将实体插入到对应的MongoDB集合中
        // 插入成功后，MongoDB会自动生成_id字段并回填到实体对象中
        entity = mongoTemplate.insert(entity);

        // 判断插入是否成功：检查_id字段是否不为null
        // 如果_id不为null，说明MongoDB成功生成了主键，插入操作成功
        return entity.get_id() != null;
    }

    public String selectIdByAppointmentNo(String appointmentNo) {
        Criteria criteria = Criteria.where("uuid").is(appointmentNo);
        Query query = new Query(criteria);
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);
        return entity.get_id();
    }

    // 查询体检人在当前科室对应的体检项目
    public List<Map> selectCheckupByPlace(String uuid, String place){

        //创建聚合管道 （相当于写了一条 sql）
        Aggregation aggregation = Aggregation.newAggregation(
                // 投影 （默认也把 _id 投影了）
                Aggregation.project("checkup", "uuid"),
                // 展开
                Aggregation.unwind("$checkup"),
                // 条件
                Aggregation.match(Criteria.where("uuid").is(uuid).and("checkup.place").is(place))
        );

        // 通过聚合管道执行查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "checkup_result", Map.class);

        //转换成我们需要的数组格式 [{], {}]
        List<Map> list = new ArrayList<>();
        results.getMappedResults().forEach(one -> {
            HashMap map = (HashMap) one.get("checkup");
            list.add(map);
        });
        return list;
    }

    // 查询体检人在当前科室的体检项目是否已经录入了体检结果
    public boolean hasCheckupResults(String uuid, String place){

        // 创建聚合管道
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("result", "uuid"),
                Aggregation.unwind("$result"),
                Aggregation.match(Criteria.where("uuid").is(uuid).and("result.place").is(place))
        );
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "checkup_result", HashMap.class);
        List<HashMap> list = results.getMappedResults();
        return list.size() > 0;
    }

    /**
     * 添加或更新体检结果
     * @param uuid 体检编号
     * @param place 科室名称
     * @param map 体检结果数据
     */
    public void addResult(String uuid, String place, Map map) {
        // 构建查询条件：根据UUID查找记录
        Criteria criteria = Criteria.where("uuid").is(uuid);
        Query query = new Query(criteria);

        // 根据UUID从MongoDB中查找对应的体检记录
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);

        // 获取该体检记录中所有已体检的科室列表
        List<String> placeList = entity.getPlace();
        // 获取该体检记录中所有的体检结果列表
        List<Map> resultList = entity.getResult();

        // 判断是否存在该科室的体检结果
        int index = 0;
        if (placeList.contains(place)) {
            // 如果该科室已经存在体检结果，找到该科室结果在列表中的位置
            for (int i = 0; i < resultList.size(); i++) {
                Map one = resultList.get(i);
                String temp = MapUtil.getStr(one, "place");
                if (place.equals(temp)) {
                    index = i;
                    break;
                }
            }
            // 用该科室新的体检结果覆盖原有结果（更新操作）
            resultList.set(index, map);
        } else {
            // 如果该科室不存在体检结果，执行新增操作
            // 在科室列表中添加新体检的科室
            placeList.add(place);
            // 在结果列表中添加新的体检结果
            resultList.add(map);
        }

        // 更新数据：将修改后的实体对象保存回MongoDB
        mongoTemplate.save(entity);
    }

    //查询体检结果
    public CheckupResultEntity selectById(String id){
        CheckupResultEntity entity = mongoTemplate.findById(id, CheckupResultEntity.class);
        return entity;
    }
}