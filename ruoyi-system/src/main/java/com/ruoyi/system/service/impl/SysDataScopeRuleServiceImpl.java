package com.ruoyi.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.dto.DataScopeRule;
import com.ruoyi.common.core.domain.dto.RoleDTO;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.enums.DataScopeType;
import com.ruoyi.common.helper.DataBaseHelper;
import com.ruoyi.common.service.IDataScopeRuleLoader;
import com.ruoyi.common.utils.StreamUtils;
import com.ruoyi.system.domain.SysRoleDataScope;
import com.ruoyi.system.domain.SysRoleDataScopeDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleDataScopeDeptMapper;
import com.ruoyi.system.mapper.SysRoleDataScopeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 功能级数据范围规则加载器
 */
@RequiredArgsConstructor
@Service
public class SysDataScopeRuleServiceImpl implements IDataScopeRuleLoader {

    private final SysRoleDataScopeMapper roleDataScopeMapper;

    private final SysRoleDataScopeDeptMapper roleDataScopeDeptMapper;

    private final SysDeptMapper deptMapper;

    @Override
    public List<DataScopeRule> loadRules(String scopeKey, Collection<RoleDTO> roles, Long deptId) {
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roles.stream()
            .map(RoleDTO::getRoleId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<SysRoleDataScope> scopes = roleDataScopeMapper.selectList(Wrappers.<SysRoleDataScope>lambdaQuery()
            .in(SysRoleDataScope::getRoleId, roleIds)
            .eq(SysRoleDataScope::getScopeKey, scopeKey));
        if (CollUtil.isEmpty(scopes)) {
            return Collections.emptyList();
        }

        List<Long> scopeIds = StreamUtils.toList(scopes, SysRoleDataScope::getId);
        Map<Long, List<Long>> customDeptMap = loadCustomDeptMap(scopeIds);

        List<DataScopeRule> rules = new ArrayList<>();
        for (SysRoleDataScope scope : scopes) {
            DataScopeRule rule = new DataScopeRule();
            rule.setDataScope(scope.getDataScope());
            String type = scope.getDataScope();
            if (DataScopeType.CUSTOM.getCode().equals(type)) {
                rule.setDeptIds(customDeptMap.getOrDefault(scope.getId(), Collections.emptyList()));
            } else if (DataScopeType.DEPT.getCode().equals(type)) {
                rule.setDeptIds(ObjectUtil.isNull(deptId) ? Collections.emptyList() : Collections.singletonList(deptId));
            } else if (DataScopeType.DEPT_AND_CHILD.getCode().equals(type)) {
                rule.setDeptIds(ObjectUtil.isNull(deptId) ? Collections.emptyList() : listDeptAndChild(deptId));
            } else {
                rule.setDeptIds(Collections.emptyList());
            }
            rules.add(rule);
        }
        return rules;
    }

    private Map<Long, List<Long>> loadCustomDeptMap(List<Long> scopeIds) {
        Map<Long, List<Long>> map = new HashMap<>();
        if (CollUtil.isEmpty(scopeIds)) {
            return map;
        }
        List<SysRoleDataScopeDept> depts = roleDataScopeDeptMapper.selectList(Wrappers.<SysRoleDataScopeDept>lambdaQuery()
            .in(SysRoleDataScopeDept::getScopeId, scopeIds));
        for (SysRoleDataScopeDept dept : depts) {
            map.computeIfAbsent(dept.getScopeId(), key -> new ArrayList<>()).add(dept.getDeptId());
        }
        return map;
    }

    private List<Long> listDeptAndChild(Long deptId) {
        List<SysDept> childDepts = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
            .select(SysDept::getDeptId)
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        List<Long> ids = StreamUtils.toList(childDepts, SysDept::getDeptId);
        ids.add(deptId);
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<SysDept> depts = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
            .select(SysDept::getDeptId)
            .in(SysDept::getDeptId, ids));
        return StreamUtils.toList(depts, SysDept::getDeptId);
    }

}
