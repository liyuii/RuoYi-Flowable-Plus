package com.ruoyi.common.core.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 功能级数据范围规则
 */
@Data
public class DataScopeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据范围：1 全部，2 自定义，3 本部门，4 本部门及以下，5 仅本人
     */
    private String dataScope;

    /**
     * 可见部门ID列表：自定义/本部门/本部门及以下由加载器解析后返回
     */
    private List<Long> deptIds;

}
