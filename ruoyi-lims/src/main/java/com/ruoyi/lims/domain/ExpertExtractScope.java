package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 专家抽取范围对象 expert_extract_scope
 */
@Data
@TableName("expert_extract_scope")
public class ExpertExtractScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 抽取申请ID */
    private Long extractId;

    /** 专业编码 */
    private String majorCode;

    /** 需要人数 */
    private Integer needNum;

    /** 抽取顺序 */
    private Integer orderNum;

    /** 专业名称，展示用，不入库 */
    @TableField(exist = false)
    private String majorName;

    /** 已抽到且有效的人数，展示用，不入库 */
    @TableField(exist = false)
    private Integer validNum;

    /** 还缺几个人，展示用，不入库 */
    @TableField(exist = false)
    private Integer shortNum;
}
