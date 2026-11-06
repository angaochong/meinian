package com.jkweilai.meinian.api.app.controller;

import com.jkweilai.meinian.api.app.service.FlowRegulationService;
import com.jkweilai.meinian.api.common.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("AppFlowRegulationController")
@RequestMapping("/app/appointment")
public class FlowRegulationController {

    @Resource
    private FlowRegulationService flowRegulationService;

    @GetMapping("/findRecommendations")
    public R findRecommendations(){
        List<Object> list = flowRegulationService.findRecommendations();
        return R.ok().put("result",list);
    }
}
