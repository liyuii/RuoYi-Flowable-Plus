package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lims_test_demo")
public class LimsTestDemo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String demoNo;

    private String demoName;

    private String demoType;

    private String content;

    private String status;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}