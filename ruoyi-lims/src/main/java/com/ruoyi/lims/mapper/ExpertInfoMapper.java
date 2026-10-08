package com.ruoyi.lims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.lims.domain.ExpertInfo;
import com.ruoyi.lims.domain.vo.ExpertMajorCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 评标专家 Mapper
 */
public interface ExpertInfoMapper extends BaseMapper<ExpertInfo> {

    /**
     * 专家库分页列表，带出专业名称
     */
    IPage<ExpertInfo> selectExpertPage(Page<ExpertInfo> page, @Param("query") ExpertInfo query);

    /**
     * 查某条范围的候选专家 ID
     * 条件：专业匹配、地区匹配、状态正常、评标当天没有有效占用、不在回避名单里、不在排除名单里
     *
     * @param majorCode    专业编码
     * @param regionCode   地区编码
     * @param evaluateDate 评标日期
     * @param extractId    抽取申请ID，用于读回避名单
     * @param excludeIds   需要排除的专家ID，抽的时候是本次已选中的人，补抽的时候还要加上本次申请出现过的人
     */
    List<Long> selectCandidateIds(@Param("majorCode") String majorCode,
                                  @Param("regionCode") String regionCode,
                                  @Param("evaluateDate") Date evaluateDate,
                                  @Param("extractId") Long extractId,
                                  @Param("excludeIds") Collection<Long> excludeIds);

    /**
     * 按专业分组统计当前可抽取的人数，口径和 selectCandidateIds 一致
     *
     * @param regionCode   地区编码
     * @param evaluateDate 评标日期
     * @param expertKeys   表单里当前填的回避专家，格式为 姓名|身份证号
     * @param companies    表单里当前填的回避单位名称
     */
    List<ExpertMajorCountVo> selectMajorCountList(@Param("regionCode") String regionCode,
                                                  @Param("evaluateDate") Date evaluateDate,
                                                  @Param("expertKeys") Collection<String> expertKeys,
                                                  @Param("companies") Collection<String> companies);
}
