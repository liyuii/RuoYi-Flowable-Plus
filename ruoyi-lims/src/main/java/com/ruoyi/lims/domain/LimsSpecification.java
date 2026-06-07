package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 规格书对象 lims_specification
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lims_specification")
public class LimsSpecification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String specName;

    private String materialName;

    private String version;

    private String status;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}
