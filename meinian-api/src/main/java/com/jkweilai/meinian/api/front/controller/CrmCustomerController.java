package com.jkweilai.meinian.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import com.jkweilai.meinian.api.front.controller.form.LoginForm;
import com.jkweilai.meinian.api.front.controller.form.ModifyCustomerForm;
import com.jkweilai.meinian.api.front.controller.form.SendSmsCodeForm;
import com.jkweilai.meinian.api.front.service.CrmCustomerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("FrontCrmCustomerController")
@RequestMapping("/front/customer")
public class CrmCustomerController {

    @Resource(name = "FrontCrmCustomerServiceImpl")
    private CrmCustomerService crmCustomerService;

    @PostMapping("/modify")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R modify(@RequestBody @Valid ModifyCustomerForm form){
        int id = StpCustomerUtil.getLoginIdAsInt();
        String phone = form.getPhone();

        boolean exist = crmCustomerService.phoneExist(phone, id);
        if(exist){
            return R.ok().put("result", false);
        }

        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("id", id);
        boolean result = crmCustomerService.modify(param);
        return R.ok().put("result", result);
    }

    @GetMapping("/logout")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R logout() {
        int loginId = StpCustomerUtil.getLoginIdAsInt();
        StpCustomerUtil.logout(loginId, "PC");
        return R.ok().put("result", true);
    }

    @GetMapping("/isLogin")
    public R isLogin(){
        boolean isLogin = StpCustomerUtil.isLogin();
        return R.ok().put("result",isLogin);
    }

    @PostMapping("/login")
    public R login (@RequestBody @Valid LoginForm form) {
        String phone = form.getPhone();
        String code = form.getCode();

        Map<String, Object> map = crmCustomerService.login(phone, code);

        Boolean result = MapUtil.getBool(map, "result");
        String msg = MapUtil.getStr(map, "msg");
        R r = R.ok().put("result", result);
        if(result){
            //生成令牌
            int id = MapUtil.getInt(map, "id");
            StpCustomerUtil.login(id, "PC");
            String token = StpCustomerUtil.getTokenValue();
            r.put("token", token);
        }
        return r;
    }

    @PostMapping("/sendSmsCode")
    public R sendSmsCode(@RequestBody @Valid SendSmsCodeForm form){
        boolean success = crmCustomerService.sendSmsCode(form.getPhone());
        String msg = success ? "短信验证码发送成功" : "短信验证码发送失败";
        return R.ok(msg).put("result", success);
    }

    @GetMapping("/getSummary")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R getSummary(){
        int id = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> summary = crmCustomerService.getSummaryById(id);
        return R.ok().put("result", summary);
    }
}
