package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.db.mapper.SysRoleMapper;
import com.jkweilai.meinian.api.mis.service.SysRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<Map<String, Object>> getAll() {
        return sysRoleMapper.selectAll();
    }
}
