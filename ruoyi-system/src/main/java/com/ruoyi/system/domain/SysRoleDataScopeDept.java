package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 角色功能数据范围自定义部门 sys_role_data_scope_dept
 */
@Data
@TableName("sys_role_data_scope_dept")
public class SysRoleDataScopeDept {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联 sys_role_data_scope.id
     */
    private Long scopeId;

    /**
     * 部门ID
     */
    private Long deptId;

    private Date createTime;

}
