package com.ruoyi.demo.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户上传模板审核请求（模拟运维系统审核，本地直接自己审）
 */
@Data
public class DemoUploadAuditBo {

    @NotNull(message = "记录ID不能为空")
    private Long id;

    /**
     * 是否通过
     */
    @NotNull(message = "请选择审核结果")
    private Boolean pass;

    /**
     * 不通过原因（不通过时必填）
     */
    private String reason;

}
