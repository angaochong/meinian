package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.MedDepartmentFlowMapper;
import com.jkweilai.meinian.api.db.pojo.MedDepartmentFlowEntity;
import com.jkweilai.meinian.api.mis.service.FlowRegulationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FlowRegulationServiceImpl implements FlowRegulationService {

    @Resource
    private MedDepartmentFlowMapper medDepartmentFlowMapper;

    @Override
    public List<String> findDepartmentNames() {
        return medDepartmentFlowMapper.selectDepartmentNames();
    }

    @Override
    public PageResult<MedDepartmentFlowEntity> pageQuery(Map<String, Object> params) {
        long total = medDepartmentFlowMapper.selectCount(params);
        List<MedDepartmentFlowEntity> records = new ArrayList<>();
        if(total > 0){
            records = medDepartmentFlowMapper.selectPage(params);
        }
        return new PageResult<>(total, records);
    }

    @Override
    @Transactional
    public int save(MedDepartmentFlowEntity entity) {
        return medDepartmentFlowMapper.insert(entity);
    }

    @Override
    public MedDepartmentFlowEntity findById(int id) {
        return medDepartmentFlowMapper.selectById(id);
    }

    @Override
    @Transactional
    public int modify(Map<String, Object> params) {
        return medDepartmentFlowMapper.update(params);
    }

    @Override
    @Transactional
    public int removeByIds(Integer[] ids) {
        return medDepartmentFlowMapper.deleteByIds(ids);
    }
}
