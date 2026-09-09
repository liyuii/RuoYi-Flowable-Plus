package com.ruoyi.lims.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 合同统计汇总
 */
@Data
public class ContractSummaryVO {

    /**
     * 合同总数
     */
    private Long totalCount;

    /**
     * 合同总金额
     */
    private BigDecimal totalAmount;

    /**
     * 已通过合同数量
     */
    private Long approvedCount;

    /**
     * 已通过合同金额
     */
    private BigDecimal approvedAmount;

}
