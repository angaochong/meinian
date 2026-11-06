package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.db.mapper.SysDepartmentMapper;
import com.jkweilai.meinian.api.mis.service.SysDeptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysDeptServiceImpl implements SysDeptService {

    @Resource
    private SysDepartmentMapper sysDepartmentMapper;

    @Override
    public List<Map<String, Object>> getAll() {
        return sysDepartmentMapper.selectAll();
    }

}
