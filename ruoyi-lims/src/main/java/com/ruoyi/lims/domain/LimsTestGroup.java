package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检测组对象 lims_test_group
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lims_test_group")
public class LimsTestGroup extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String groupCode;

    private String status;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}
