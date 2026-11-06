package com.jkweilai.meinian.api.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.IdcardUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.config.satoken.StpCustomerUtil;
import com.jkweilai.meinian.api.db.pojo.MedExamAppointmentEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.front.controller.form.PageQueryAppointmentForm;
import com.jkweilai.meinian.api.front.controller.form.SaveAppointmentForm;
import com.jkweilai.meinian.api.front.service.AppointmentService;
import com.jkweilai.meinian.api.front.service.TradeOrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController("FrontAppointmentController")
@RequestMapping("/front/appointment")
public class AppointmentController {

    @Resource(name = "FrontTradeOrderServiceImpl")
    private TradeOrderService tradeOrderService;

    @Resource(name = "FrontAppointmentServiceImpl")
    private AppointmentService appointmentService;

    @PostMapping("/save")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R save(@RequestBody @Valid SaveAppointmentForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> param = new HashMap() {{
            put("customerId", customerId);
            put("orderId", form.getOrderId());
        }};
        boolean bool = tradeOrderService.hasOwnOrder(param);
        if (!bool) {
            throw new HisException("预约失败，该订单与您无关");
        }

        // 验证身份证是否有效
        String idCardNo = form.getIdCardNo();
        if (!IdcardUtil.isValidCard18(idCardNo)) {
            throw new HisException("身份证号码无效");
        }
        String birthDate = IdcardUtil.getBirthDate(idCardNo).toDateStr();
        String gender = IdcardUtil.getGenderByIdCard(idCardNo) == 1 ? "男" : "女";

        // 验证日期是否为未来60天以内
        DateTime appointmentDate = DateUtil.parse(form.getAppointmentDate());
        DateTime tomorrow = DateUtil.tomorrow(); //当前时刻的24小时之后
        DateTime startDate = DateUtil.parse(tomorrow.toDateStr()); //明天凌晨
        DateTime endDate = tomorrow.offset(DateField.DAY_OF_MONTH, 60);
        boolean temp = appointmentDate.isIn(startDate, endDate);
        if (!temp) {
            throw new HisException("预约日期错误");
        }
        MedExamAppointmentEntity entity = BeanUtil.toBean(form, MedExamAppointmentEntity.class);
        entity.setAppointmentNo(IdUtil.simpleUUID().toUpperCase());
        entity.setBirthDate(birthDate);
        entity.setGender(gender);

        String result = appointmentService.save(entity);
        return R.ok().put("result", result);
    }

    @PostMapping("/pageQuery")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R pageQuery(@RequestBody @Valid PageQueryAppointmentForm form){
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        form.setCustomerId(customerId);
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = appointmentService.pageQueryByCondition(param);
        return R.ok().put("pageResult", pageResult);
    }
}
