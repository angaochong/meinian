package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.db.pojo.MedDepartmentFlowEntity;
import com.jkweilai.meinian.api.mis.controller.form.*;
import com.jkweilai.meinian.api.mis.service.FlowRegulationService;
import com.jkweilai.meinian.api.mis.service.SysConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/flow_regulation")
public class FlowRegulationController {

    @Resource
    private FlowRegulationService flowRegulationService;

    @Resource
    private SysConfigService sysConfigService;

    @GetMapping("/findDepartmentNames")
    @SaCheckLogin
    public R findDepartmentNames() {
        List<String> deptNames = flowRegulationService.findDepartmentNames();
        return R.ok().put("result", deptNames);
    }

    @GetMapping("/findFlowRule")
    @SaCheckLogin
    public R findFlowRule() {
        String result = sysConfigService.getItemValue("auto_flow_regulation");
        return R.ok().put("result", Boolean.parseBoolean(result));
    }

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid FlowRegulationPageQueryForm form){
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = pageSize * (pageNo - 1);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<MedDepartmentFlowEntity> pageResult = flowRegulationService.pageQuery(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/save")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:INSERT"}, mode = SaMode.OR)
    public R insert(@RequestBody @Valid SaveFlowRegulationForm form){
        MedDepartmentFlowEntity entity = BeanUtil.toBean(form, MedDepartmentFlowEntity.class);
        int rows = flowRegulationService.save(entity);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/findById")
    @SaCheckPermission(value = {"ROOT", "FOLW__rEGULATION:SELECT"}, mode = SaMode.OR)
    public R findById(@RequestBody @Valid FindMedDepartmentFlowByIdForm form){
        MedDepartmentFlowEntity entity = flowRegulationService.findById(form.getId());
        return R.ok().put("result", entity);
    }

    @PostMapping("/modify")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULACTION:UPDATE"}, mode = SaMode.OR)
    public R modify(@RequestBody @Valid ModifyMedDepartmentFlowForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = flowRegulationService.modify(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/removeByIds")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:DELETE"}, mode = SaMode.OR)
    public R removeByIds(@RequestBody @Valid RemoveMedDepartmentFlowByIdsForm form){
        int rows = flowRegulationService.removeByIds(form.getIds());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/switchFlowRegulation")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION"}, mode = SaMode.OR)
    public R switchFlowRegulation(@RequestBody @Valid SwitchFlowRegulationModeForm form){
        boolean result = sysConfigService.setConfigValue("auto_flow_regulation", String.valueOf(form.getMode()));
        return R.ok().put("result", result);
    }
}
