package com.jkweilai.meinian.api.async;

import com.jkweilai.meinian.api.mis.service.MedExamReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CheckupWorkAsync {

    @Resource
    private MedExamReportService medExamReportService;

    @Async("AsyncTaskExecutor")
    public void createReport(Integer id){
        medExamReportService.createReport(id);
    }
}
