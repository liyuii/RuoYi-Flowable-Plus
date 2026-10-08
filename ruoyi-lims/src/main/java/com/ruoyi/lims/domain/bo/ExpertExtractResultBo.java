package com.ruoyi.lims.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 专家抽取结果状态变更对象
 */
@Data
public class ExpertExtractResultBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 结果ID */
    private Long id;

    /** 目标状态：JOIN参加 / QUIT不参加 / LEAVE请假 */
    private String status;

    /** 理由，请假必填 */
    private String reason;
}
