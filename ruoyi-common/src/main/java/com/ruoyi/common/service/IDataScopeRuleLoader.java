package com.ruoyi.common.service;

import com.ruoyi.common.core.domain.dto.DataScopeRule;
import com.ruoyi.common.core.domain.dto.RoleDTO;

import java.util.Collection;
import java.util.List;

/**
 * 功能级数据范围规则加载器
 */
public interface IDataScopeRuleLoader {

    /**
     * 加载当前用户在指定功能下的数据范围规则
     *
     * @param scopeKey 功能范围标识
     * @param roles    当前用户角色
     * @param deptId   当前用户部门ID
     * @return 规则列表，空表示无规则
     */
    List<DataScopeRule> loadRules(String scopeKey, Collection<RoleDTO> roles, Long deptId);

}
