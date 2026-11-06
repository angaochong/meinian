package com.jkweilai.meinian.api.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.common.MinIO;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.mapper.MedExamPackageMapper;
import com.jkweilai.meinian.api.db.pojo.MedExamPackageEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.service.MedExamPackageService;
import jakarta.annotation.Resource;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.*;

@Service
public class MedExamPackageServiceImpl implements MedExamPackageService {

    @Resource
    private MedExamPackageMapper medExamPackageMapper;

    @Override
    public PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param) {
        long total = medExamPackageMapper.selectCountByCondition(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if(total > 0){
            records = medExamPackageMapper.selectPageByCondition(param);
        }
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        Integer pageNo = MapUtil.getInt(param, "pageNo");
        PageResult<Map<String, Object>> pageResult = new PageResult<>(total, pageSize, pageNo, records);
        return pageResult;
    }

    @Resource
    private MinIO minIO;

    @Override
    public String uploadImg(MultipartFile file) {
        String fileName = IdUtil.simpleUUID() + ".jpg";
        String path = "front/goods/" + fileName;
        minIO.uploadImage(path, file);
        return path;
    }

    @Override
    @Transactional
    public int save(MedExamPackageEntity entity) {
        // 将主要字段生成md5 hash
        String md5Hash = generateMd5Hash(entity);
        // 将md5值赋给entity对象的md5Hash属性
        entity.setMd5Hash(md5Hash);
        return medExamPackageMapper.insert(entity);
    }

    @Override
    public Map<String, Object> findById(Integer id) {
        Map<String, Object> param = new HashMap<>(Map.of("id", id));
        Map<String, Object> map = medExamPackageMapper.selectById(param);
        String[] columns = {"departmentExam", "labExam", "medicalExam", "otherExam", "tags"};
        for(String column : columns){
            String temp = MapUtil.getStr(map, column);
            if(temp != null){
                JSONArray array = JSONUtil.parseArray(temp);
                map.replace(column, array);
            }
        }
        return map;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#entity.id")
    public int modify(MedExamPackageEntity entity) {
        String md5Hash = generateMd5Hash(entity);
        entity.setMd5Hash(md5Hash);
        return medExamPackageMapper.update(entity);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#medExamPackageId")
    public void uploadExamItems(Integer medExamPackageId, MultipartFile file) {
        // 解析excel文件，将数据保存到套餐对象
        List<Map<String, String>> list = new ArrayList<>();
        try(BufferedInputStream in = new BufferedInputStream(file.getInputStream())){
            // 获取整个excel文件
            XSSFWorkbook book = new XSSFWorkbook(in);
            // 获取第一个sheet页面：一共就一页。
            XSSFSheet sheet = book.getSheetAt(0);
            // excel第一行是表头，从第二行开始循环
            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                // 取出当前行
                XSSFRow row = sheet.getRow(i);
                // 一共8列，一列一列取出
                XSSFCell cell1 = row.getCell(0);
                String cell1Value = cell1.getStringCellValue();

                XSSFCell cell2 = row.getCell(1);
                String cell2Value = cell1.getStringCellValue();

                XSSFCell cell3 = row.getCell(2);
                String cell3Value = cell1.getStringCellValue();

                XSSFCell cell4 = row.getCell(3);
                String cell4Value = cell1.getStringCellValue();

                XSSFCell cell5 = row.getCell(4);
                String cell5Value = cell1.getStringCellValue();

                XSSFCell cell6 = row.getCell(5);
                String cell6Value = cell1.getStringCellValue();

                XSSFCell cell7 = row.getCell(6);
                String cell7Value = cell1.getStringCellValue();

                XSSFCell cell8 = row.getCell(7);
                String cell8Value = cell1.getStringCellValue();

                // 将单元格的数据封装到Map集合中。一个Map集合相当于一条记录。
                Map<String, String> examItem = new LinkedHashMap<>();
                examItem.put("place", cell1Value);
                examItem.put("name", cell2Value);
                examItem.put("item", cell3Value);
                examItem.put("type", cell4Value);
                examItem.put("code", cell5Value);
                examItem.put("sex", cell6Value);
                examItem.put("value", cell7Value);
                examItem.put("template", cell8Value);

                // 将Map添加到List集合
                list.add(examItem);
            }
        }catch (Exception e){
            throw new HisException("解析Excel出错了");
        }
        // TODO 将excel文件上传到minio服务器
        // TODO 根据id查询套餐对象
        // TODO 重新生成md5Hash值
        // TODO 更新套餐数据到数据库表

        // 将excel文件上传到minio服务器
        String path = "mis/goods/exam_items/" + medExamPackageId + ".xlsx";
        minIO.uploadExcel(path, file);

        MedExamPackageEntity medExamPackageIdEntity = medExamPackageMapper.selectEntityById(medExamPackageId);
        String examItems = JSONUtil.parseArray(list).toString();
        medExamPackageIdEntity.setExamItems(examItems);
        String md5Hash = generateMd5Hash(medExamPackageIdEntity);

        Map<String,Object> param = new HashMap<>(){{
            put("examItems",examItems);
            put("md5Hash",md5Hash);
            put("id", medExamPackageId);
        }};

        int rows = medExamPackageMapper.updateExamItems(param);
        if(rows != 1){
            throw new HisException("更新体检内容失败");
        }

    }

    @Override
    @Transactional
    //套餐下架就剔除
    @CacheEvict(cacheNames = "goods", key = "#params.get('id')", condition = "#params.get('status') == false ")
    public int modifyStatus(Map<String, Object> params) {
        return medExamPackageMapper.updateStatus(params);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "goods", key = "#ids")
    public int removeByIds(Integer[] ids) {
        List<String> images = medExamPackageMapper.selectImagesByIds(ids);
        int rows = medExamPackageMapper.deleteByIds(ids);
        if(rows > 0){
            images.forEach(image -> {
                minIO.deleteFile(image);
            });
        }
        return rows;
    }

    private String generateMd5Hash(MedExamPackageEntity entity) {
        // 将entity对象转换成json对象
        JSONObject json = JSONUtil.parseObj(entity);
        // 将无关紧要的属性移除，因为这些属性在生成md5 hash时是非必要属性
        json.remove("id");
        json.remove("categoryId");
        json.remove("salesVolume");
        json.remove("status");
        json.remove("md5Hash");
        json.remove("createTime");
        json.remove("updateTime");
        // 生成md5 hash并返回
        return MD5.create().digestHex(json.toString()).toUpperCase();
    }
}
