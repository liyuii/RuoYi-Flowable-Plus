package com.ruoyi.lims.vo;

import lombok.Data;

/**
 * 合同状态统计
 */
@Data
public class ContractStatusStatVO {

    /**
     * 状态：0 草稿，1 审批中，3 已驳回，9 已通过
     */
    private String status;

    /**
     * 合同数量
     */
    private Long count;

}
