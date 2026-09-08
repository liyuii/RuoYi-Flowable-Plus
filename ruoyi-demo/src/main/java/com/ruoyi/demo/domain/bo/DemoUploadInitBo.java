package com.ruoyi.demo.domain.bo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 初始化上传请求
 */
@Data
public class DemoUploadInitBo {

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
