package com.jkweilai.meinian.api.mis.controller;

import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.mis.service.SysDeptService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/dept")
public class DeptController {

    @Resource
    private SysDeptService sysDeptService;

    @GetMapping("/listDepts")
    public R listDepts(){
        List<Map<String, Object>> list = sysDeptService.getAll();
        return R.ok().put("list", list);
    }
}
