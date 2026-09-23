package com.ruoyi.lims.mapper;

import com.ruoyi.lims.domain.ContractApprove;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.annotation.DataColumn;
import com.ruoyi.common.annotation.DataPermission;
import com.ruoyi.lims.vo.ContractMonthlyStatVO;
import com.ruoyi.lims.vo.ContractStatusStatVO;
import com.ruoyi.lims.vo.ContractSummaryVO;
import com.ruoyi.lims.vo.ContractTypeStatVO;

import java.util.List;

public interface ContractApproveMapper extends BaseMapper<ContractApprove> {

    @DataPermission(scopeKey = "contract:statistics", value = {
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    ContractSummaryVO selectContractSummary();

    @DataPermission(scopeKey = "contract:statistics", value = {
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    List<ContractStatusStatVO> selectContractStatusStat();

    @DataPermission(scopeKey = "contract:statistics", value = {
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    List<ContractTypeStatVO> selectContractTypeStat();

    @DataPermission(scopeKey = "contract:statistics", value = {
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    List<ContractMonthlyStatVO> selectContractMonthlyStat();

}
