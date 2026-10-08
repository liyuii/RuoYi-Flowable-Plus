package com.ruoyi.lims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.lims.domain.ExpertExtractScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家抽取范围 Mapper
 */
public interface ExpertExtractScopeMapper extends BaseMapper<ExpertExtractScope> {

    /**
     * 按申请查范围，带出专业名称，按顺序排
     */
    List<ExpertExtractScope> selectScopeListByExtractId(@Param("extractId") Long extractId);
}
