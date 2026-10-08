package com.ruoyi.lims.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.lims.domain.ExpertExtract;
import com.ruoyi.lims.domain.ExpertExtractAvoid;
import com.ruoyi.lims.domain.ExpertExtractScope;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 专家抽取申请新增/修改对象
 * 主表字段继承 ExpertExtract，另外带上范围明细和回避名单
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertExtractBo extends ExpertExtract {

    private static final long serialVersionUID = 1L;

    /** 抽取范围明细 */
    @TableField(exist = false)
    private List<ExpertExtractScope> scopes;

    /** 回避名单 */
    @TableField(exist = false)
    private List<ExpertExtractAvoid> avoids;
}
