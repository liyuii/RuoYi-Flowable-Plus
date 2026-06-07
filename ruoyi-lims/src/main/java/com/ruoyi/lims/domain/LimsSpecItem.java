package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 规格书检测项目定义对象 lims_spec_item
 */
@Data
@TableName("lims_spec_item")
public class LimsSpecItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long specId;

    private String itemName;

    private String itemMethod;

    private BigDecimal specLower;

    private BigDecimal specUpper;

    private String unit;

    private Long testGroupId;

    private String testGroupName;

    private Integer sortOrder;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    private Date createTime;

    private Date updateTime;
}
