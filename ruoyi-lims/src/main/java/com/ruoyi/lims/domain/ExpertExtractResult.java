package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 专家抽取结果对象 expert_extract_result
 */
@Data
@TableName("expert_extract_result")
public class ExpertExtractResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 抽取申请ID */
    private Long extractId;

    /** 抽取范围ID */
    private Long scopeId;

    /** 专家ID */
    private Long expertId;

    /** 评标日期，冗余自申请，用于占用判断 */
    private Date evaluateDate;

    /** 专家状态：WAIT_CONFIRM待确认 / JOIN参加 / QUIT不参加 / LEAVE请假 */
    private String status;

    /** 不参加或请假的理由 */
    private String reason;

    /** 抽取轮次 */
    private Integer roundNo;

    /** 抽取类型：FIRST首次抽取 / SUPPLEMENT补充抽取 */
    private String extractType;

    /** 抽取操作人 */
    private String extractBy;

    /** 抽取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date extractTime;

    /** 状态变更人 */
    private String statusBy;

    /** 状态变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date statusTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
