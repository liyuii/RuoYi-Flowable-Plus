package com.ruoyi.demo.domain.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 初始化上传请求
 */
@Data
public class DemoUploadInitBo {

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    /**
     * 项目金额（元）
     */
    @NotNull(message = "项目金额不能为空")
    @DecimalMin(value = "0", message = "项目金额不能为负数")
    private BigDecimal amount;

    /**
     * 项目所在省份行政区划代码
     */
    @NotBlank(message = "请选择所在省份")
    @Pattern(regexp = "\\d{6}", message = "请选择正确的省份")
    private String provinceCode;

    /**
     * 重传时传原记录ID：只有“审核不通过”的记录允许重传
     */
    private Long replaceId;

    /**
     * 原始文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件总大小，字节
     */
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于 0")
    private Long fileSize;

    /**
     * 整个文件 SHA-256
     */
    @NotBlank(message = "文件哈希不能为空")
    private String fileHash;

}
