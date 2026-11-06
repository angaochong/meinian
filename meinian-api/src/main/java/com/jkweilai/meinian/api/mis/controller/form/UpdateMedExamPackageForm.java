package com.jkweilai.meinian.api.mis.controller.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class UpdateMedExamPackageForm {
    @NotNull(message = "id不能为空")
    @Min(value = 1, message = "id不能小于1")
    private Integer id;

    @NotBlank(message = "packageCode不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "packageCode内容不正确")
    private String packageCode;

    @NotBlank(message = "packageName不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,50}$", message = "packageName内容不正确")
    private String packageName;

    @NotBlank(message = "description不能为空")
    @Length(max = 200, message = "description不能超过200个字符")
    private String description;

    @Valid
    private ArrayList<ExamForm> departmentExam;

    @Valid
    private ArrayList<ExamForm> labExam;

    @Valid
    private ArrayList<ExamForm> medicalExam;

    @Valid
    private ArrayList<ExamForm> otherExam;

    @NotBlank(message = "coverImage不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z/\\.]{1,200}$", message = "coverImage内容不正确")
    private String coverImage;

    @NotNull(message = "originalPrice不能为空")
    @Min(value = 0, message = "originalPrice不能小于0")
    private BigDecimal originalPrice;

    @NotNull(message = "currentPrice不能为空")
    @Min(value = 0, message = "currentPrice不能小于0")
    private BigDecimal currentPrice;

    @NotBlank(message = "packageType不能为空")
    @Pattern(regexp = "^父母体检$|^入职体检$|^职场白领$|^个人高端$|^中青年体检$")
    private String packageType;

    private String[] tags;

    @Range(min = 1, max = 5, message = "categoryId范围不正确")
    private Integer categoryId;

    @Min(value = 1, message = "promotionId不能小于1")
    private Integer promotionId;

}
