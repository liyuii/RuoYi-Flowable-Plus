package com.ruoyi.lims.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 某个专业当前可抽取的人数
 */
@Data
public class ExpertMajorCountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 专业编码 */
    private String majorCode;

    /** 可抽取人数 */
    private Integer availableNum;
}
