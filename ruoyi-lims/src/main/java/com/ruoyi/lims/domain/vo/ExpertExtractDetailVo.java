package com.ruoyi.lims.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.lims.domain.ExpertExtract;
import com.ruoyi.lims.domain.ExpertExtractAvoid;
import com.ruoyi.lims.domain.ExpertExtractScope;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 专家抽取申请详情，主表字段直接继承，另外带上范围、回避名单、结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertExtractDetailVo extends ExpertExtract {

    private static final long serialVersionUID = 1L;

    /** 抽取范围明细，带上已抽到人数和缺额 */
    @TableField(exist = false)
    private List<ExpertExtractScope> scopes;

    /** 回避名单 */
    @TableField(exist = false)
    private List<ExpertExtractAvoid> avoids;

    /** 抽取结果 */
    @TableField(exist = false)
    private List<ExpertExtractResultVo> results;
}
