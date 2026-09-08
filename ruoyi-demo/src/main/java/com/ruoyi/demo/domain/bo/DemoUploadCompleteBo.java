package com.ruoyi.demo.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 合并完成请求
 */
@Data
public class DemoUploadCompleteBo {

    /**
     * 上传会话编号
     */
    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    /**
     * 整个文件 SHA-256
     */
    private String fileHash;

}
