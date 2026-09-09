package com.ruoyi.lims.vo;

import lombok.Data;

import java.util.List;

/**
 * 合同统计结果
 */
@Data
public class ContractStatisticsVO {

    /**
     * 顶部汇总指标
     */
    private ContractSummaryVO summary;

    /**
     * 状态分布
     */
    private List<ContractStatusStatVO> statusList;

    /**
     * 类型分布
     */
    private List<ContractTypeStatVO> typeList;

    /**
     * 月度趋势
     */
    private List<ContractMonthlyStatVO> monthlyList;

}
