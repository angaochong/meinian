package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.jkweilai.meinian.api.common.MinIO;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.dao.CheckupResultDao;
import com.jkweilai.meinian.api.db.dao.GoodsSnapshotDao;
import com.jkweilai.meinian.api.db.mapper.MedExamAppointmentMapper;
import com.jkweilai.meinian.api.db.mapper.MedExamReportMapper;
import com.jkweilai.meinian.api.db.mapper.TradeOrderMapper;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.service.AppointmentService;
import com.jkweilai.meinian.api.util.FaceAuthUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private MedExamAppointmentMapper medExamAppointmentMapper;

    @Resource
    private FaceAuthUtil  faceAuthUtil;

    @Resource
    private MinIO minIO;

    @Resource
    private CheckupResultDao checkupResultDao;

    @Resource
    private GoodsSnapshotDao goodsSnapshotDao;

    @Resource
    private TradeOrderMapper  tradeOrderMapper;

    @Resource
    private MedExamReportMapper medExamReportMapper;

    @Override
    public List<Map<String, Object>> findByOrderId(Integer orderId) {
        return medExamAppointmentMapper.selectByOrderId(orderId);
    }

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        List<Map<String, Object>> records = new ArrayList<>();
        long total = medExamAppointmentMapper.selectCount(param);
        if(total > 0){
            records = medExamAppointmentMapper.selectPage(param);
        }
        return new PageResult<>(total, records);
    }

    @Override
    @Transactional
    public int removeByIds(Integer[] ids) {
        return medExamAppointmentMapper.deleteByIds(ids);
    }

    @Override
    public int hasAppointmentInToday(Map<String, Object> param) {
        Map<String, Object> map = medExamAppointmentMapper.selectAppointmentInToday(param);
        if(map == null){
            //没有预约
            return 0;
        }else if(MapUtil.getInt(map, "status") != 1){
            //有预约，已经签到了
            return -1;
        }else{
            //有预约，没签到
            return 1;
        }
    }

    @Override
    @Transactional
    public boolean checkin(Map<String, Object> param) {
        String idCardNo = MapUtil.getStr(param, "idCardNo");
        String patientName = MapUtil.getStr(param, "patientName");
        String gender = IdcardUtil.getGenderByIdCard(idCardNo) == 1 ? "男" : "女";
        String photo_1 = MapUtil.getStr(param, "photo_1");
        String photo_2 = MapUtil.getStr(param, "photo_2");

        /*boolean result = faceAuthUtil.verifyFaceModel(patientName, idCardNo, gender, photo_1, photo_2);

        if(result){
            String fileName = idCardNo + "_" + new Date().getTime() + ".jpg";
            String path  = "checkin/" + fileName;

            minIO.uploadImage(path, photo_2);

            int rows = medExamAppointmentMapper.updateForCheckin(param);
            if(rows != 1){
                throw new HisException("保存签到记录失败");
            }

            Map<String, Object> map = medExamAppointmentMapper.selectAppointNoAndSnapshotId(param);
            String appointmentNo = MapUtil.getStr(map, "appointmentNo");
            String snapshotId = MapUtil.getStr(map, "snapshotId");

            List<Map> checkup = goodsSnapshotDao.searchCheckup(snapshotId, gender);

            boolean bool = checkupResultDao.insert(appointmentNo, checkup);
            if(!bool){
                throw new HisException("添加体检结果失败");
            }
        }

        return result;*/

        //模拟,没有热恋识别，没有保存体检人图片到minio
        boolean result = true;
        if(result){
            int rows = medExamAppointmentMapper.updateForCheckin(param);
            if(rows != 1){
                throw new HisException("保存签到记录失败");
            }
            Map<String, Object> map = medExamAppointmentMapper.selectAppointNoAndSnapshotId(param);
            String appointmentNo = MapUtil.getStr(map, "appointmentNo");
            String snapshotId = MapUtil.getStr(map, "snapshotId");

            List<Map> checkup = goodsSnapshotDao.searchCheckup(snapshotId, gender);

            boolean bool = checkupResultDao.insert(appointmentNo, checkup);
            if(!bool){
                throw new HisException("添加体检结果失败");
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> findGuidanceInfo(Integer id) {
        Map<String, Object> map = medExamAppointmentMapper.selectSummaryById(id);

        String snapshotId = MapUtil.getStr(map, "snapshotId");
        String gender = MapUtil.getStr(map, "gender");
        String appointmentNo = MapUtil.getStr(map, "appointmentNo");

        QrConfig qrConfig = new QrConfig();
        qrConfig.setWidth(100);
        qrConfig.setHeight(100);
        qrConfig.setMargin(0);

        //用流水号做二维码（Base64格式）
        String qrCodeBase64 = QrCodeUtil.generateAsBase64(appointmentNo, qrConfig, "jpg");
        map.put("qrCodeBase64", qrCodeBase64);

        List<Map> list = goodsSnapshotDao.searchCheckup(snapshotId, gender);

        LinkedHashSet<Map> set = new LinkedHashSet();

        list.forEach(item -> {
            HashMap temp = new HashMap(){
                {
                    put("place", MapUtil.getStr(item, "place"));
                    put("name", MapUtil.getStr(item, "name"));
                }
            };
            set.add(temp);
        });

        map.put("checkup", set);

        return map;
    }

    @Override
    @Transactional
    public boolean modifyStatusByAppointmentNo(Map<String, Object> param) {
        int rows = medExamAppointmentMapper.updateStatusByAppointmentNo(param);
        if(rows != 1){
            return false;
        }

        Integer status = MapUtil.getInt(param, "status");
        if(status == 3){
            String appointmentNo = MapUtil.getStr(param, "appointmentNo");
            Map<String, Object> map = tradeOrderMapper.selectOrderIsFinished(appointmentNo);
            Integer orderId = MapUtil.getInt(map, "orderId");
            Integer n1 = MapUtil.getInt(map, "n1");
            Integer n2 = MapUtil.getInt(map, "n2");
            if(n1.equals(n2)){
                rows = tradeOrderMapper.updateStatus(Map.of("orderStatus", 6, "orderId", orderId));
                if(rows != 1){
                    return false;
                }
            }

            String reportId = checkupResultDao.selectIdByAppointmentNo(appointmentNo);
            param.put("reportId", reportId);
            rows = medExamReportMapper.insert(param);
            if(rows != 1){
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> findByAppointmentNo(String appointmentNo) {
        Map<String, Object> map = medExamAppointmentMapper.selectByAppointmentNo(appointmentNo);

        if(map == null){
            Map<String, Object> result = new HashMap<>();
            result.put("error", "没有预约记录");
            return result;
        }

        int status = MapUtil.getInt(map, "status");
        if(status == 1){
            Map<String, Object> result = new HashMap<>();
            result.put("error", "体检人未签到");
            return result;
        }

        if(status == 4){
            Map<String, Object> result = new HashMap<>();
            result .put("error", "该体检人已取消");
            return result;
        }

        return map;
    }
}
