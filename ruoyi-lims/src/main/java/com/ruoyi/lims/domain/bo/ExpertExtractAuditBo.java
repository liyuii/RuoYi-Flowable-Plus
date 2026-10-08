package com.ruoyi.lims.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 专家抽取申请审核对象
 */
@Data
public class ExpertExtractAuditBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long id;

    /** 是否通过 */
    private Boolean pass;

    /** 审核意见 */
    private String opinion;
}
