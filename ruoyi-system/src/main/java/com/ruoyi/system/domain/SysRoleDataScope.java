package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 角色功能数据范围 sys_role_data_scope
 */
@Data
@TableName("sys_role_data_scope")
public class SysRoleDataScope {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 功能范围标识
     */
    private String scopeKey;

    /**
     * 数据范围：1 全部，2 自定义，3 本部门，4 本部门及以下，5 仅本人
     */
    private String dataScope;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

}
