package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.jkweilai.meinian.api.common.MinIO;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.db.pojo.MedExamPackageEntity;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.controller.form.*;
import com.jkweilai.meinian.api.mis.service.MedExamPackageService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/mis/goods")
@RequiredArgsConstructor
public class MedExamPackageController {

    @Resource
    private MedExamPackageService medExamPackageService;

    private final MinIO minIO;


    @PostMapping("/removeByIds")
    @SaCheckPermission(value = {"ROOT", "GOODS:DELETE"}, mode = SaMode.OR)
    public R removeByIds(@RequestBody @Valid RemoveMedExamPackageForm form) {
        Integer[] ids = form.getIds();
        int rows = medExamPackageService.removeByIds(ids);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/modifyStatus")
    @SaCheckPermission(value = {"ROOT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public R modifyStatus(@RequestBody @Valid ModifyStatusForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = medExamPackageService.modifyStatus(param);
        return R.ok().put("result", rows);
    }

    @PostMapping("/downloadExamItems")
    @SaCheckPermission(value = {"ROOT","GOODS:SELECT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public void downloadExamItems(@RequestBody @Valid DownloadExamItemsForm form, HttpServletResponse response){
        //设置下载文件的名称
        response.setHeader("Content-Disposition",
                "attachment;filename=" + form.getId() + ".xlsx");
        //该MIME类型会让浏览器弹出下载对话框
        response.setContentType("application/x-download");
        response.setCharacterEncoding("UTF-8");
        String path = "mis/goods/exam_items/" + form.getId() + ".xlsx";
        try (
                //读取Minio存储文件的输入流

                InputStream in = minIO.downloadFile(path);
                BufferedInputStream bin = new BufferedInputStream(in);
                //向响应输出数据的输出流
                ServletOutputStream out = response.getOutputStream();
                BufferedOutputStream bout = new BufferedOutputStream(out);) {
            //把输入流中的数据拷贝到输出流
            IoUtil.copy(bin, bout);
        } catch (Exception e) {
            throw new HisException("文档下载失败");
        }
    }

    @PostMapping("/uploadExamItems")
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public R uploadExamItems(@Valid UploadExamItemsForm form, @RequestParam("file") MultipartFile file) {
        medExamPackageService.uploadExamItems(form.getId(), file);
        return R.ok();
    }

    @PostMapping("/modify")
    @SaCheckPermission(value = {"ROOT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public R modify(@RequestBody @Valid UpdateMedExamPackageForm form){
        // 将form对象转换成entity，但是在转换的时候自动忽略5个属性，因为这5个属性我们需要手动调用set方法赋值。
        MedExamPackageEntity entity = BeanUtil.toBean(form, MedExamPackageEntity.class, CopyOptions.create()
                .setIgnoreProperties("departmentExam", "labExam", "medicalExam", "otherExam", "tags"));

        if (form.getDepartmentExam() != null && !form.getDepartmentExam().isEmpty()) {
            String departmentExam = JSONUtil.toJsonStr(form.getDepartmentExam());
            entity.setDepartmentExam(departmentExam);
        }
        if (form.getLabExam() != null && !form.getLabExam().isEmpty()) {
            String labExam = JSONUtil.toJsonStr(form.getLabExam());
            entity.setLabExam(labExam);
        }
        if (form.getMedicalExam() != null && !form.getMedicalExam().isEmpty()) {
            String medicalExam = JSONUtil.toJsonStr(form.getMedicalExam());
            entity.setMedicalExam(medicalExam);
        }
        if (form.getOtherExam() != null && !form.getOtherExam().isEmpty()) {
            String otherExam = JSONUtil.toJsonStr(form.getOtherExam());
            entity.setOtherExam(otherExam);
        }
        if (form.getTags() != null && form.getTags().length > 0) {
            String tags = JSONUtil.toJsonStr(form.getTags());
            entity.setTags(tags);
        }

        int rows = medExamPackageService.modify(entity);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/findById")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT"}, mode = SaMode.OR)
    public R findById(@RequestBody @Valid FindMedExamPackageByIdForm form){
        Map<String, Object> result = medExamPackageService.findById(form.getId());
        return R.ok().put("result", result);
    }

    @PostMapping("/save")
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT"}, mode = SaMode.OR)
    public R save(@RequestBody @Valid SaveMedExamPackageForm form) {
        MedExamPackageEntity entity = BeanUtil.toBean(form, MedExamPackageEntity.class, CopyOptions.create()
                .setIgnoreProperties("departmentExam", "labExam", "medicalExam", "otherExam", "tags"));
        if (form.getDepartmentExam() != null && !form.getDepartmentExam().isEmpty()) {
            entity.setDepartmentExam(JSONUtil.toJsonStr(form.getDepartmentExam()));
        }
        if (form.getLabExam() != null && !form.getLabExam().isEmpty()) {
            entity.setLabExam(JSONUtil.toJsonStr(form.getLabExam()));
        }
        if (form.getMedicalExam() != null && !form.getMedicalExam().isEmpty()) {
            entity.setMedicalExam(JSONUtil.toJsonStr(form.getMedicalExam()));
        }
        if (form.getOtherExam() != null && !form.getOtherExam().isEmpty()) {
            entity.setOtherExam(JSONUtil.toJsonStr(form.getOtherExam()));
        }
        if (form.getTags() != null && form.getTags().length > 0) {
            entity.setTags(JSONUtil.toJsonStr(form.getTags()));
        }

        int rows = medExamPackageService.save(entity);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/uploadImage")
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public R uploadImage(@RequestParam @Valid MultipartFile file) {
        String path = medExamPackageService.uploadImg(file);
        return R.ok().put("result", path);
    }

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid ExamPackagePageQueryForm form) {
        Integer pageSize = form.getPageSize();
        Integer pageNo = form.getPageNo();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = medExamPackageService.pageQueryByCondition(param);
        return R.ok().put("pageResult", pageResult);
    }
}
