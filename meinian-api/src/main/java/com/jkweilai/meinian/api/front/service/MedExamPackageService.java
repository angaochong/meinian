package com.jkweilai.meinian.api.front.service;

import com.jkweilai.meinian.api.common.PageResult;

import java.util.List;
import java.util.Map;

public interface MedExamPackageService {

    //根据id查找套餐
    Map<String, Object> findById(Integer id);

    //根据categoryIds查找套餐
    Map<Integer, List<Map<String, Object>>> findTop4(Integer[] categoryIds);

    //分页查询front商品列表
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //根据快照id查询商品快照
    Map<String, Object> findBySnapshotId(String snapshotId, Integer customerId);
}
