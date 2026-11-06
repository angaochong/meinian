package com.jkweilai.meinian.api.front.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.dao.GoodsSnapshotDao;
import com.jkweilai.meinian.api.db.mapper.MedExamPackageMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.db.pojo.GoodsSnapshotEntity;
import com.jkweilai.meinian.api.front.service.MedExamPackageService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("FrontMedExamPackageServiceImpl")
public class MedExamPackageServiceImpl implements MedExamPackageService {

    @Resource
    private MedExamPackageMapper medExamPackageMapper;
    @Autowired
    private GoodsSnapshotDao goodsSnapshotDao;
    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Override
    @Cacheable(cacheNames ="goods", key = "#id")
    public Map<String, Object> findById(Integer id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("status", 1);
        Map<String, Object> map = medExamPackageMapper.selectById(params);
        // 将查询结果中的体检项目字符串全部转换为json对象
        if (map != null) {
            for (String key : new String[]{"tags", "departmentExam", "labExam", "medicalExam", "otherExam"}) {
                String temp = MapUtil.getStr(map, key);
                JSONArray array = JSONUtil.parseArray(temp);
                map.replace(key, array);
            }
            return map;
        }
        return null;
    }

    @Override
    public Map<Integer, List<Map<String, Object>>> findTop4(Integer[] categoryIds) {
        Map<Integer, List<Map<String, Object>>> map = new HashMap<>();
        for(Integer categoryId : categoryIds){
            List<Map<String, Object>> list = medExamPackageMapper.selectTop4(categoryId);
            map.put(categoryId, list);
        }
        return map;
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        List<Map<String, Object>> records = new ArrayList<>();
        long count = medExamPackageMapper.selectCountForFront(param);
        if(count > 0){
            records = medExamPackageMapper.selectPageForFront(param);
        }
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        //返回当前的分页参数数据
        PageResult<Map<String, Object>> pageResult = new PageResult<>(count, pageNo, pageSize, records);
        return pageResult;
    }

    @Override
    public Map<String, Object> findBySnapshotId(String snapshotId, Integer customerId) {

        //根据snapshotId和customerId查找orderId，看客户有没有这个订单
        if(customerId != null){
            Integer orderId = tradeOrderMapper.selectByCustomerIdAndSnapshotId(snapshotId, customerId);
            if(orderId == null){
                return null;
            }
        }

        GoodsSnapshotEntity entity = goodsSnapshotDao.findById(snapshotId);
        Map<String, Object> map = BeanUtil.beanToMap(entity);
        return map;
    }
}
