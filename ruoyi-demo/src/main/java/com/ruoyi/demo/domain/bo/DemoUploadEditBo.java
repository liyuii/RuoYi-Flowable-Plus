package com.ruoyi.demo.domain.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 编辑用户上传模板（只改表单信息，不换文件；换文件走重传的 initUpload）
 */
@Data
public class DemoUploadEditBo {

    @NotNull(message = "记录ID不能为空")
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotNull(message = "项目金额不能为空")
    @DecimalMin(value = "0", message = "项目金额不能为负数")
    private BigDecimal amount;

    @NotBlank(message = "请选择所在省份")
    @Pattern(regexp = "\\d{6}", message = "请选择正确的省份")
    private String provinceCode;

}
