package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.front.service.CustomerImService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mis/customer/im")
public class CustomerImController {

    @Resource(name = "FrontCustomerImServiceImpl")
    private CustomerImService customerImService;

    @GetMapping("/getServiceAccount")
    @SaCheckPermission(value = {"ROOT", "CUSTOMER_IM:SELECT"}, mode = SaMode.OR)
    public R getServiceAccount() {
        Map<String, Object> result = customerImService.getServiceAccount();
        return R.ok().put("result", result);
    }
}
