package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.mis.controller.form.*;
import com.jkweilai.meinian.api.mis.service.AppointmentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @PostMapping("/findByOrderId")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R findByOrderId(@RequestBody @Valid FindAppointmentByOrderIdForm form) {
        List<Map<String, Object>> list = appointmentService.findByOrderId(form.getOrderId());
        return R.ok().put("result", list);
    }

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid PageQueryAppointmentForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = appointmentService.pageQueryByCondition(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/deleteByIds")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:DELETE"}, mode = SaMode.OR)
    public R deleteByIds(@RequestBody @Valid RemoveAppointmentByIdsForm form) {
        int rows = appointmentService.removeByIds(form.getIds());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/hasInToday")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:UPDATE"}, mode = SaMode.OR)
    public R hasInToday(@RequestBody @Valid HasAppointmentInTodayForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int result = appointmentService.hasAppointmentInToday(param);
        return R.ok().put("result", result);
    }

    @PostMapping("/checkin")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:UPDATE"}, mode = SaMode.OR)
    public R checkin(@RequestBody @Valid CheckinAppointmentForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        boolean result = appointmentService.checkin(param);
        return R.ok().put("result", result);
    }

    @PostMapping("/findGuidanceInfo")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R findGuidanceInfo(@RequestBody @Valid FindGuidanceInfoForm form) {
        Map<String, Object> map = appointmentService.findGuidanceInfo(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/modifyStatusByAppointmentNo")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:UPDATE"}, mode = SaMode.OR)
    public R modifyStatusByAppointmentNo(@RequestBody @Valid ModifyStatusByAppointmentNoForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        boolean result = appointmentService.modifyStatusByAppointmentNo(param);
        return R.ok().put("result", result);
    }

    @PostMapping("/findByAppointmentNo")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R findByAppointmentNo(@RequestBody @Valid FindAppointmentByAppointmentNoForm form) {
        Map<String, Object> map = appointmentService.findByAppointmentNo(form.getAppointmentNo());
        return R.ok().put("result", map);
    }
}
