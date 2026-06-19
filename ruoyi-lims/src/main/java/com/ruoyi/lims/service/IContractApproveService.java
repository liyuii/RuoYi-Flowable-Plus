package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ContractApprove;
import java.util.Collection;
import java.util.List;

public interface IContractApproveService {
    TableDataInfo<ContractApprove> queryPageList(ContractApprove bo, PageQuery pageQuery);
    List<ContractApprove> queryList(ContractApprove bo);
    ContractApprove queryById(Long id);
    Boolean insert(ContractApprove bo);
    Boolean update(ContractApprove bo);
    Boolean deleteWithValidByIds(Collection<Long> ids);
}