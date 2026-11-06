package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.controller.form.OrderPageQueryForm;
import com.jkweilai.meinian.api.mis.controller.form.ProcessOfflineRefundForm;
import com.jkweilai.meinian.api.mis.controller.form.RemoveOrderByIdForm;
import com.jkweilai.meinian.api.mis.controller.form.SyncPaymentResultForm;
import com.jkweilai.meinian.api.mis.service.TradeOrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mis/order")
public class TradeOrderController {

    @Resource
    private TradeOrderService tradeOrderService;

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid OrderPageQueryForm form) {
        //开始日期和结束日期的情况必须一致，要有都有，要没有都没有
        String startDate = form.getStartDate();
        String endDate = form.getEndDate();
        if ((!StrUtil.isBlank(startDate) && StrUtil.isBlank(endDate)) || (StrUtil.isBlank(startDate) && !StrUtil.isBlank(endDate))) {
            throw new HisException("开始日期和结束日期要么都是空，要么都不为空！");
        } else if (!StrUtil.isBlank(startDate) && !StrUtil.isBlank(endDate)) {
            DateTime start = DateUtil.parse(startDate, "yyyy-MM-dd");
            DateTime end = DateUtil.parse(endDate, "yyyy-MM-dd");
            if (start.isAfter(end)) {
                throw new HisException("开始日期必须在结束日期前面");
            }
        }

        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);

        PageResult<Map<String, Object>> pageResult = tradeOrderService.pageQueryByCondition(params);

        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/syncPaymentResult")
    @SaCheckPermission(value = {"ROOT", "ORDER:UPDATE"}, mode = SaMode.OR)
    public R syncPaymentResult(@RequestBody @Valid SyncPaymentResultForm form) {
        String[] outTradeNos = form.getOutTradeNos();
        int rows = tradeOrderService.syncPaymentResult(outTradeNos);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/removeById")
    @SaCheckPermission(value = {"ROOT", "ORDER:DELETE"}, mode = SaMode.OR)
    public R removeById(@RequestBody @Valid RemoveOrderByIdForm form){
        int rows = tradeOrderService.removeById(form.getOrderId());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/offlineRefund")
    @SaCheckPermission(value = {"ROOT", "ORDER:UPDATE"}, mode = SaMode.OR)
    public R offlineRefund(@RequestBody @Valid ProcessOfflineRefundForm form){
        String outTradeNo = form.getOutTradeNo();
        boolean success = tradeOrderService.processOfflineRefund(outTradeNo);
        return R.ok().put("success", success);
    }

}