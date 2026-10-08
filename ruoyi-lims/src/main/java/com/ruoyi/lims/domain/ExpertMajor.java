package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 专家专业关联对象 expert_major
 */
@Data
@TableName("expert_major")
public class ExpertMajor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 专家ID */
    private Long expertId;

    /** 专业编码 */
    private String majorCode;
}
