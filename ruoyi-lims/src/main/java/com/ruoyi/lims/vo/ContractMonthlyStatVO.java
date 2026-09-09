package com.ruoyi.lims.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 合同月度统计
 */
@Data
public class ContractMonthlyStatVO {

    /**
     * 月份，格式 yyyy-MM
     */
    private String month;

    /**
     * 当月合同数量
     */
    private Long count;

    /**
     * 当月合同金额
     */
    private BigDecimal amount;

}
