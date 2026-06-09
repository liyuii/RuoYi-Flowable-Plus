package com.ruoyi.lims.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsInspection;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.lims.domain.LimsSpecItem;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.lims.mapper.LimsInspectionMapper;
import com.ruoyi.lims.mapper.LimsSpecItemMapper;
import com.ruoyi.lims.mapper.LimsTestItemMapper;
import com.ruoyi.lims.service.ILimsInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsInspectionServiceImpl implements ILimsInspectionService {
    private final LimsInspectionMapper baseMapper;
    private final LimsSpecItemMapper specItemMapper;
    private final LimsTestItemMapper testItemMapper;
    private final FlowServiceFactory flowServiceFactory;
    @Override
    public TableDataInfo<LimsInspection> queryPageList(LimsInspection bo, PageQuery pageQuery) {
        Page<LimsInspection> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }
    @Override
    public List<LimsInspection> queryList(LimsInspection bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }
    private LambdaQueryWrapper<LimsInspection> buildQueryWrapper(LimsInspection bo) {
        LambdaQueryWrapper<LimsInspection> lqw = new LambdaQueryWrapper<>();
        lqw.like(ObjectUtil.isNotEmpty(bo.getInspectionNo()), LimsInspection::getInspectionNo, bo.getInspectionNo());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getSpecId()), LimsInspection::getSpecId, bo.getSpecId());
        lqw.like(ObjectUtil.isNotEmpty(bo.getBatchNo()), LimsInspection::getBatchNo, bo.getBatchNo());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), LimsInspection::getStatus, bo.getStatus());
        return lqw;
    }
    @Override public LimsInspection queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(LimsInspection bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(LimsInspection bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
