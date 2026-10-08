package com.ruoyi.lims.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 抽取预检结果，一条范围一条
 */
@Data
public class ExpertDrawCheckVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 范围ID */
    private Long scopeId;

    /** 专业编码 */
    private String majorCode;

    /** 专业名称 */
    private String majorName;

    /** 需要人数 */
    private Integer needNum;

    /** 当前可用人数 */
    private Integer availableNum;

    /** 还缺几个人，0 表示够 */
    private Integer shortNum;
}
