package com.ruoyi.system.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 保存角色功能数据范围
 */
@Data
public class SysRoleDataScopeBo {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 功能数据范围规则列表
     */
    private List<SysDataScopeRuleDto> rules;

}
