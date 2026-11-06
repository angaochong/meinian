package com.jkweilai.meinian.api.db.mapper;

import com.jkweilai.meinian.api.db.pojo.MedExamPackageEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【med_exam_package(体检套餐表)】的数据库操作Mapper
* @createDate 2026-07-09 14:26:46
* @Entity com.jkweilai.meinian.api.db.pojo.MedExamPackageEntity
*/
public interface MedExamPackageMapper {

    //分页查询返回的总记录条数
    long selectCountByCondition(Map<String, Object> params);

    //分页查询的数据
    List<Map<String, Object>> selectPageByCondition(Map<String, Object> params);

    //新增套餐
    int insert(MedExamPackageEntity entity);

    //根据id查找套餐
    Map<String, Object> selectById(Map<String, Object> param);

    //更新套餐
    int update(MedExamPackageEntity entity);

    //根据id获取套餐数据
    MedExamPackageEntity selectEntityById(Integer id);

    //更新exam_items字段
    int updateExamItems(Map<String, Object> params);

    //更新套餐状态
    int updateStatus(Map<String, Object> param);

    //根据id查找图片在存储同的（相对）路径
    List<String> selectImagesByIds(Integer[] ids);

    //根据id删除套餐
    int deleteByIds(Integer[] ids);

    //获取体检套餐
    List<Map<String, Object>> selectTop4(Integer categoryId);

    //分页查询商品列表
    List<Map<String, Object>> selectPageForFront(Map<String, Object> params);

    //查询结果的总记录条数
    long selectCountForFront(Map<String, Object> params);

    //获取商品信息和促销规则
    Map<String, Object> selectPackageAndPromotionById(Integer id);

    //更新商品销量
    int updateSalesVolume(@Param("goodsId") Integer goodsId, @Param("buyCount") Integer buyCount);

}




