package com.ruoyi.lims.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.mapper.ContractApproveMapper;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.lims.service.IContractApproveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
@RequiredArgsConstructor
@Service
public class ContractApproveServiceImpl implements IContractApproveService {
    private final ContractApproveMapper baseMapper;
    @Override
    public TableDataInfo<ContractApprove> queryPageList(ContractApprove bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ContractApprove> contractApproveLambdaQueryWrapper = buildQueryWrapper(bo);
        Page<ContractApprove> page = baseMapper.selectPage(pageQuery.build(), contractApproveLambdaQueryWrapper);
        return TableDataInfo.build(page);
    }
    @Override
    public List<ContractApprove> queryList(ContractApprove bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<ContractApprove> buildQueryWrapper(ContractApprove bo) {
        LambdaQueryWrapper<ContractApprove> lqw = new LambdaQueryWrapper<>();
        lqw.like(ObjectUtil.isNotEmpty(bo.getContractNo()), ContractApprove::getContractNo, bo.getContractNo());
        lqw.like(ObjectUtil.isNotEmpty(bo.getContractName()), ContractApprove::getContractName, bo.getContractName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), ContractApprove::getStatus, bo.getStatus());
        lqw.orderByDesc(ContractApprove::getCreateTime);
        lqw.eq(ContractApprove::getCreateBy, LoginHelper.getUsername());
        return lqw;
    }
    @Override public ContractApprove queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(ContractApprove bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(ContractApprove bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
