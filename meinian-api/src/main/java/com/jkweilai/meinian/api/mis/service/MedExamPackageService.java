package com.jkweilai.meinian.api.mis.service;

import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.db.pojo.MedExamPackageEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MedExamPackageService {

    //分页查询结果
    PageResult<Map<String, Object>> pageQueryByCondition(Map<String, Object> param);

    //上传图片
    String uploadImg(MultipartFile file);

    //新增套餐
    int save(MedExamPackageEntity entity);

    //根据id查找套餐
    Map<String, Object> findById(Integer id);

    //更新套餐
    int modify(MedExamPackageEntity entity);

    //上传excle文件到MinIO
    void uploadExamItems(Integer medExamPackageId, MultipartFile file);

    //更新套餐状态
    int modifyStatus(Map<String, Object> params);

    //删除套餐，并且要删除MinIO中的图片
    int removeByIds(Integer[] ids);
}
