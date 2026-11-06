package com.jkweilai.meinian.api.mis.service.impl;

import com.jkweilai.meinian.api.db.dao.CheckupResultDao;
import com.jkweilai.meinian.api.mis.service.CheckupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckupServiceImpl implements CheckupService {

    @Resource
    private CheckupResultDao checkupResultDao;

    @Override
    public Map findCheckupByPlace(String uuid, String place) {
        List<Map> checkupList = checkupResultDao.selectCheckupByPlace(uuid, place);
        boolean hasCheckupResults = checkupResultDao.hasCheckupResults(uuid, place);
        HashMap<String, Object> map = new HashMap<>(){{
            put("hasCheckupResults", hasCheckupResults);
            put("checkupList", checkupList);
        }};
        return map;
    }

    @Override
    public void addResult(String uuid, String place, Map map) {
        checkupResultDao.addResult(uuid, place, map);
    }
}
