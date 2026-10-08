package com.ruoyi.lims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.lims.domain.ExpertExtractResult;
import com.ruoyi.lims.domain.vo.ExpertExtractResultVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家抽取结果 Mapper
 */
public interface ExpertExtractResultMapper extends BaseMapper<ExpertExtractResult> {

    /**
     * 按申请查结果，带出专家信息和专业名称
     */
    List<ExpertExtractResultVo> selectResultListByExtractId(@Param("extractId") Long extractId);
}
