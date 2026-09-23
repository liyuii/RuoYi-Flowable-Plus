package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 功能数据范围标识 sys_data_scope_define
 */
@Data
@TableName("sys_data_scope_define")
public class SysDataScopeDefine {

    /**
     * 功能范围标识
     */
    @TableId(value = "scope_key", type = IdType.INPUT)
    private String scopeKey;

    /**
     * 范围名称
     */
    private String scopeName;

    /**
     * 状态：0 启用，1 停用
     */
    private String status;

    private String remark;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

}
