package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.db.pojo.PromotionRuleEntity;
import com.jkweilai.meinian.api.mis.controller.form.*;
import com.jkweilai.meinian.api.mis.service.PromotionRuleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mis/rule")
public class PromotionRuleController {

    @Resource
    private PromotionRuleService promotionRuleService;

    @GetMapping("list")
    public R list(){
        List<Map<String, Object>> list = promotionRuleService.findAll();
        return R.ok().put("result", list);
    }

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid RulePageQueryForm form){
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = promotionRuleService.pageQueryByCondition(param);
        return R.ok().put("pageResult", pageResult);
    }


    @PostMapping("/save")
    @SaCheckPermission(value = {"ROOT", "RULE:INSERT"}, mode = SaMode.OR)
    public R save(@RequestBody @Valid SavePromotionRuleForm form){
        PromotionRuleEntity promotionRuleEntity = BeanUtil.toBean(form, PromotionRuleEntity.class);
        int rows = promotionRuleService.save(promotionRuleEntity);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/findById")
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    public R findById(@RequestBody @Valid FindRuleByIdForm form){
        PromotionRuleEntity rule = promotionRuleService.findById(form.getRuleId());
        return R.ok().put("result", rule);
    }

    @PostMapping("/modify")
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    public R modify(@RequestBody @Valid ModifyRuleForm form){
        PromotionRuleEntity promotionRuleEntity = BeanUtil.toBean(form, PromotionRuleEntity.class);
        int rows = promotionRuleService.modifyById(promotionRuleEntity);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/removeById")
    @SaCheckPermission(value = {"ROOT", "RULE:DELETE"}, mode = SaMode.OR)
    public R removeById(@RequestBody @Valid RemoveRuleByIdForm form){
        Integer ruleId = form.getRuleId();
        int rows = promotionRuleService.removeById(ruleId);
        return R.ok().put("rows", rows);
    }
}
