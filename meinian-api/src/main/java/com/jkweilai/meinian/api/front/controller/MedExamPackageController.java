package com.jkweilai.meinian.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import com.jkweilai.meinian.api.db.mapper.MedExamPackageMapper;
import com.jkweilai.meinian.api.front.controller.form.FindGoodsBySnapshotIdForm;
import com.jkweilai.meinian.api.front.controller.form.FindMedExamPackageByIdForm;
import com.jkweilai.meinian.api.front.controller.form.FindTop4ByCategoryIdsForm;
import com.jkweilai.meinian.api.front.controller.form.MedExamPackagePageQueryForm;
import com.jkweilai.meinian.api.front.service.MedExamPackageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/front/goods")
@RestController("FrontMedExamPackageController")
public class MedExamPackageController {

    @Resource(name = "FrontMedExamPackageServiceImpl")
    private MedExamPackageService medExamPackageService;

    @PostMapping("/pageQuery")
    public R pageQuery(@RequestBody @Validated MedExamPackagePageQueryForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = pageSize * (pageNo - 1);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = medExamPackageService.pageQueryByCondition(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("findTop4ByCategoryIds")
    public R findTop4ByCategoryIds(@Validated @RequestBody FindTop4ByCategoryIdsForm form) {
        Map<Integer, List<Map<String, Object>>> map = medExamPackageService.findTop4(form.getCategoryIds());
        return R.ok().put("result", map);
    }

    @PostMapping("/findById")
    public R findById(@RequestBody @Valid FindMedExamPackageByIdForm form){
        Map<String, Object> map = medExamPackageService.findById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/findSnapshotForMis")
    @SaCheckLogin
    public R findSnapshotForMis(@RequestBody @Validated FindGoodsBySnapshotIdForm form){
        Map<String, Object> map = medExamPackageService.findBySnapshotId(form.getSnapshotId(), null);
        return R.ok().put("result", map);
    }


    @PostMapping("/findSnapshotForFront")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R findSnapshotForFront(@RequestBody @Validated FindGoodsBySnapshotIdForm form){
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> map = medExamPackageService.findBySnapshotId(form.getSnapshotId(), customerId);
        return R.ok().put("result", map);
    }
}
