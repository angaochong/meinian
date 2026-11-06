package com.jkweilai.meinian.api.mis.controller;

import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.mis.service.SysRoleService;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/role")
public class RoleController {

    @Resource
    private SysRoleService sysRoleService;

    @GetMapping("/listRoles")
    public R listRoles(){
        List<Map<String, Object>> list = sysRoleService.getAll();
        return R.ok().put("list", list);
    }
}
