package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 检测项目结果表对象 lims_test_item
 */
@Data
@TableName("lims_test_item")
public class LimsTestItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inspectionId;

    private String itemName;

    private String itemMethod;

    private BigDecimal specLower;

    private BigDecimal specUpper;

    private String resultValue;

    private String unit;

    private String qcResult;

    private Long testGroupId;

    private String testGroupName;

    private String assignee;

    @TableField(exist = false)
    private String groupMemberIds;

    private Date detectTime;

    private String status;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    private Date createTime;

    private Date updateTime;
}
