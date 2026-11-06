package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.mis.controller.form.FindCustomerSummaryForm;
import com.jkweilai.meinian.api.mis.service.CrmCustomerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mis/customer")
public class CrmCustomerController {

    @Resource
    private CrmCustomerService crmCustomerService;

    @PostMapping("/findSummary")
    @SaCheckLogin
    public R findSummary(@RequestBody @Valid FindCustomerSummaryForm form) {
        Map<String, Object> map = crmCustomerService.findSummary(form.getCustomerId());
        return R.ok().put("result", map);
    }

}
