package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.mis.controller.form.AddCheckupResultForm;
import com.jkweilai.meinian.api.mis.controller.form.FindCheckupByAppointmentNoAndPlaceForm;
import com.jkweilai.meinian.api.mis.service.CheckupService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mis/checkup")
public class CheckupController {

    @Resource
    private CheckupService checkupService;

    @PostMapping("/searchCheckupByPlace")
    @SaCheckPermission(value = {"ROOT", "CHECKUP:SELECT"}, mode = SaMode.OR)
    public R searchCheckupByPlace(@RequestBody @Valid FindCheckupByAppointmentNoAndPlaceForm form) {
        Map map = checkupService.findCheckupByPlace(form.getAppointmentNo(), form.getPlace());
        return R.ok().put("result", map);
    }

    @PostMapping("/addResult")
    @SaCheckPermission(value = {"ROOT", "CHECKUP:INSERT", "CHECKUP:UPDATE"}, mode = SaMode.OR)
    public R addResult(@RequestBody @Valid AddCheckupResultForm form){
        Map map = new HashMap();
        map.put("date", DateUtil.today());
        map.put("doctorName", form.getDoctorName());
        map.put("doctorId", StpUtil.getLoginIdAsInt());
        map.put("place", form.getPlace());
        map.put("template", form.getTemplate());
        map.put("item", form.getItem());
        checkupService.addResult(form.getAppointmentNo(), form.getPlace(), map);

        return R.ok().put("result", true);
    }
}
