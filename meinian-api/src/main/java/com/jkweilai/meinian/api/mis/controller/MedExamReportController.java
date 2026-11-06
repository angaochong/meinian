package com.jkweilai.meinian.api.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.jkweilai.meinian.api.common.MinIO;
import com.jkweilai.meinian.api.common.PageResult;
import com.jkweilai.meinian.api.common.R;
import com.jkweilai.meinian.api.exception.HisException;
import com.jkweilai.meinian.api.mis.controller.form.CreateMedExamReportForm;
import com.jkweilai.meinian.api.mis.controller.form.PageQueryMedExamReportForm;
import com.jkweilai.meinian.api.mis.controller.form.SaveWaybillForm;
import com.jkweilai.meinian.api.mis.service.MedExamReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/mis/report")
public class MedExamReportController {

    @Resource
    private MedExamReportService medExamReportService;

    @Resource
    private MinIO minIO;

    @PostMapping("/pageQuery")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@RequestBody @Valid PageQueryMedExamReportForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = medExamReportService.pageQuery(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/createReport")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:CREATE"}, mode = SaMode.OR)
    public R createReport(@RequestBody @Valid CreateMedExamReportForm form) {
        //这里手动调用创建报告，占用主线程
        boolean result = medExamReportService.createReport(form.getId());
        return R.ok().put("result", result);
    }

    // name参数是体检人的名字，以体检人的名字作为文件名
    @GetMapping("/downloadReport")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:SELECT"}, mode = SaMode.OR)
    public void downloadReport(@RequestParam String fileUrl, @RequestParam String name, HttpServletResponse response) {
        // 这个功能没有编写form，因此校验放到了这里。
        if (StrUtil.isBlank(name)) {
            throw new HisException("name不能为空");
        } else if (!ReUtil.isMatch("^[\\u4e00-\\u9fa5]{2,10}$", name)) {
            throw new HisException("name内容不正确");
        }

        try (InputStream in = minIO.downloadFile(fileUrl); OutputStream out = response.getOutputStream()) {
            // 为了保证下载正常，清除一下响应头，框架中的响应设置，拦截器中的响应设置全部清除掉（如果有的话）
            response.reset();
            // 设置响应的内容类型，文件下载的响应内容类型固定
            response.setContentType("application/x-msdownloadoctet-stream;charset=utf-8");
            // HTTP响应协议中filename不允许出现中文，所以必须编码。
            response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(name + "的体检报告.docx", "UTF-8"));

            IOUtil.copyCompletely(in, out);

            // spring框架也有自带的，也能用
            // FileCopyUtils.copy(in, out);
        } catch (Exception e) {
            throw new HisException("下载体检报告失败");
        }
    }

    @SneakyThrows
    @PostMapping("/recognizeWaybill")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:UPDATE"}, mode = SaMode.OR)
    public R recognizeWaybill(@RequestParam("file") MultipartFile file) {
        InputStream in = file.getInputStream();
        BufferedImage image = ImgUtil.read(in);
        String imageBase64 = "data:image/jpg;base64," + ImgUtil.toBase64(image, "jpg");
        in.close();
        Map<String, Object> map = medExamReportService.recognizeWaybill(imageBase64);
        return R.ok().put("result", map);
    }

    @PostMapping("/saveWaybill")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:UPDATE"}, mode = SaMode.OR)
    public R saveWaybill(@RequestBody @Valid SaveWaybillForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("status", 3);
        boolean result = medExamReportService.saveWaybill(param);
        return R.ok().put("result", result);
    }
}
