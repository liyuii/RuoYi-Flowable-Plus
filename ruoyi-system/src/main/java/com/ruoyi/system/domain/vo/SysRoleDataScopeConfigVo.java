package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.SysDataScopeDefine;
import com.ruoyi.system.domain.bo.SysDataScopeRuleDto;
import lombok.Data;

import java.util.List;

/**
 * 角色功能数据范围配置
 */
@Data
public class SysRoleDataScopeConfigVo {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 已配置规则
     */
    private List<SysDataScopeRuleDto> configuredRules;

    /**
     * 可添加功能
     */
    private List<SysDataScopeDefine> candidateScopes;

}
