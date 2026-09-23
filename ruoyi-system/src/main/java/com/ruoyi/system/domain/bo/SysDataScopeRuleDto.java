package com.ruoyi.system.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * 功能数据范围规则
 */
@Data
public class SysDataScopeRuleDto {

    /**
     * 功能范围标识
     */
    private String scopeKey;

    /**
     * 范围名称，仅返回时使用
     */
    private String scopeName;

    /**
     * 数据范围：1 全部，2 自定义，3 本部门，4 本部门及以下，5 仅本人
     */
    private String dataScope;

    /**
     * 自定义部门ID列表
     */
    private List<Long> deptIds;

}
