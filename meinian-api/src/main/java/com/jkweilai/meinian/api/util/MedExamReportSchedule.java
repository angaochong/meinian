package com.jkweilai.meinian.api.util;

import com.jkweilai.meinian.api.async.CheckupWorkAsync;
import com.jkweilai.meinian.api.db.mapper.MedExamReportMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MedExamReportSchedule {

    @Resource
    private CheckupWorkAsync checkupWorkAsync;

    @Resource
    private MedExamReportMapper medExamReportMapper;

    /**
     * 每天凌晨1~5点，每隔20分钟，生成一批10天前的体检报告。
     * 如果体检结果数据并未录入结束，自动把该体检信息发送给相关领导
     */
    //@Scheduled(cron = "0 0,20,40 1,2,3,4,5 * * ?")
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void createReport(){
        List<Integer> list = medExamReportMapper.select50NonReport();
        if(list == null || list.size() == 0){
            return;
        }

        //让异步线程去生成体检报告
        list.forEach(item->{
            checkupWorkAsync.createReport(item);
        });
        log.debug("对" + list.size() + "份体检生成报告");
    }
}
