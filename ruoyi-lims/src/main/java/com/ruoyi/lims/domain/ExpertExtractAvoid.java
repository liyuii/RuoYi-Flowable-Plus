package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 专家抽取回避名单对象 expert_extract_avoid
 */
@Data
@TableName("expert_extract_avoid")
public class ExpertExtractAvoid implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 抽取申请ID */
    private Long extractId;

    /** 回避类型：EXPERT专家 / COMPANY单位 */
    private String avoidType;

    /** 专家姓名，avoidType=EXPERT 时使用 */
    private String expertName;

    /** 身份证号，avoidType=EXPERT 时使用 */
    private String idCard;

    /** 单位名称，avoidType=COMPANY 时使用 */
    private String companyName;
}
