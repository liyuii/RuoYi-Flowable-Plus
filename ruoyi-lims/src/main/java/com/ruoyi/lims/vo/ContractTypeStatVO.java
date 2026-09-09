package com.ruoyi.lims.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 合同类型统计
 */
@Data
public class ContractTypeStatVO {

    /**
     * 合同类型：1 采购，2 销售，3 服务
     */
    private String contractType;

    /**
     * 合同数量
     */
    private Long count;

    /**
     * 合同金额合计
     */
    private BigDecimal amount;

}
