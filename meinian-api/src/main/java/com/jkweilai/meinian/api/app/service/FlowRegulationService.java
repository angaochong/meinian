package com.jkweilai.meinian.api.app.service;

import java.util.List;

public interface FlowRegulationService {

    //查询redis中的推荐科室列表
    List<Object> findRecommendations();
}
