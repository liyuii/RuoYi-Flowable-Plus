package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 专家抽取申请对象 expert_extract
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert_extract")
public class ExpertExtract extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String projectName;

    /** 标段名称，多个用逗号分隔，只做记录 */
    private String sectionNames;

    /** 抽取地区编码 */
    private String regionCode;

    /** 评标时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date evaluateTime;

    /** 状态：DRAFT/WAIT_AUDIT/WAIT_EXTRACT/EXTRACTED/WAIT_SUPPLEMENT/REJECTED */
    private String status;

    /** 申请人 */
    private String applyBy;

    /** 提交申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 备注 */
    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}
