package com.jkweilai.meinian.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import com.jkweilai.meinian.api.front.service.CustomerImService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("FrontCustomerImController")
@RequestMapping("/front/customer/im")
public class CustomerImController {

    @Resource(name = "FrontCustomerImServiceImpl")
    private CustomerImService customerImService;

    @GetMapping("/createAccount")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R createAccount(){
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> result = customerImService.createAccount(customerId);
        return R.ok().put("result", result);
    }
}
