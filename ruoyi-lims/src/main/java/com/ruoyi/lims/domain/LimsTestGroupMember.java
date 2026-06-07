package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 检测组成员对象 lims_test_group_member
 */
@Data
@TableName("lims_test_group_member")
public class LimsTestGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    private String userName;

    private Integer sortOrder;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    private Date createTime;

    private Date updateTime;
}
