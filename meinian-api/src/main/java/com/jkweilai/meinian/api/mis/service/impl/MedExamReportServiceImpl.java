package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.jkweilai.meinian.api.common.MinIO;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.dao.CheckupResultDao;
import com.jkweilai.meinian.api.db.mapper.MedExamAppointmentMapper;
import com.jkweilai.meinian.api.db.mapper.MedExamReportMapper;
import com.jkweilai.meinian.api.db.pojo.CheckupResultEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.service.MedExamReportService;
import com.jkweilai.meinian.api.util.CheckupReportUtil;
import com.jkweilai.meinian.api.util.OcrUtil;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Slf4j
@Service
public class MedExamReportServiceImpl implements MedExamReportService {

    @Resource
    private MedExamReportMapper medExamReportMapper;

    @Resource
    private MedExamAppointmentMapper  medExamAppointmentMapper;

    @Resource
    private CheckupResultDao checkupResultDao;

    @Resource
    private CheckupReportUtil checkupReportUtil;

    @Resource
    private MinIO minIO;

    @Resource
    private OcrUtil ocrUtil;

    @Override
    public PageResult<Map<String, Object>> pageQuery(Map<String, Object> param) {
        long total = medExamReportMapper.selectCount(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if(total > 0){
            records = medExamReportMapper.selectPage(param);
        }
        return new PageResult<>(total, records);
    }

    // @SneakyThrows 是 Lombok 库 提供的一个注解，它的主要作用是悄无声息地抛出受检异常（checked exceptions），而无需在方法上使用 throws 声明。
    @SneakyThrows
    @Override
    @Transactional
    public boolean createReport(Integer id) {
        Map<String, Object> result = medExamReportMapper.selectById(id);
        if(result == null || result.size() == 0){
            throw new HisException("不存在体检报告记录");
        }
        Integer appointmentId = MapUtil.getInt(result, "appointmentId");
        String reportId = MapUtil.getStr(result, "reportId");
        Integer status = MapUtil.getInt(result, "status");
        DateTime examDate = DateUtil.parseDate(MapUtil.getStr(result, "examDate"));
        DateTime today = new DateTime(DateUtil.today());

        if(today.offsetNew(DateField.DAY_OF_MONTH, -10).isBefore(examDate)){
            throw new HisException("无法生成距体检日期10天以内的报告");
        }

        if(status != 1){
            log.debug("主键为" + id + "的体检报告已经生成，当前任务自动结束");
            return true;
        }

        //查询体检人的预约信息
        HashMap<String, Object> reportData = (HashMap)medExamAppointmentMapper.selectDataForReport(appointmentId);
        //查询MongoDB中的体检项目
        CheckupResultEntity entity = checkupResultDao.selectById(reportId);
        // 所有的体检项目都完成了，才能生成体检报告
        List<Map> checkup = entity.getCheckup();
        //set集合里存储的是已经做过的体检科室的名字，set集合去重
        Set<String> set = new HashSet<>();
        checkup.forEach(item -> {
            String place = MapUtil.getStr(item, "place");
            set.add(place);
        });
        //判断已经做过的体检科室集合和实体类中的全部的科室集合大小
        //如果为true，证明没有完成体检
        if(set.size() < entity.getPlace().size()){
            log.error("有的体检项没有做，不能生成报告");
            // 这里必须发送短信给负责人。。。。。。
            return false;
        }
        //将体检内容排序，让相同科室名字紧挨着，因为生成文档的poi工具类中会把紧挨着相同的科室名字的单元格合并
        Collections.sort(checkup, (map1, map2) -> {
            String place1 = MapUtil.getStr(map1, "place");
            String place2 = MapUtil.getStr(map2, "place");
            return place1.compareTo(place2);
        });

        //生成体检报告，把这两个也添加到reportData中
        reportData.put("checkup", checkup);
        reportData.put("result", entity.getResult());

        try{
            XWPFDocument document = checkupReportUtil.createReport(reportData);

            //将报告输出到输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            outputStream.flush();

            //再将输出流读入到输入流中
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();

            //将体检报告上传到minio服务器
            String fileUrl = "front/checkup/report/" + reportId + ".docx";
            minIO.uploadWord(fileUrl, inputStream);
            inputStream.close();

            //更新体检报告表
            Map<String, Object> params = new HashMap<>();
            params.put("status", 2);
            params.put("fileUrl", fileUrl);
            params.put("id", id);
            int rows = medExamReportMapper.update(params);
            if(rows != 1){
                log.error("体检报告更新失败");
                return false;
            }
        } catch (Exception e) {
            log.error("生成体检报告失败");
            throw new HisException("生成体检报告失败", e);
        }

        //程序能够执行到这里，说明报告已经成功生成。
        return true;
    }

    @Override
    public Map<String, Object> recognizeWaybill(String imageBase64) {
        try{
            Map<String, Object> map = ocrUtil.identifyWaybill(imageBase64);
            return map;
        }catch(Exception e){
            log.error("OCR识别异常", e);
            throw new HisException("OCR识别异常");
        }
    }

    @Override
    @Transactional
    public boolean saveWaybill(Map<String, Object> param) {
        int rows = medExamReportMapper.update(param);
        return rows == 1;
    }


}
