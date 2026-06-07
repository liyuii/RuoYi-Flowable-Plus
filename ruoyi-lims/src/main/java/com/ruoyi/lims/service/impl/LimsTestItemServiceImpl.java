package com.ruoyi.lims.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.lims.mapper.LimsTestItemMapper;
import com.ruoyi.lims.service.ILimsTestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsTestItemServiceImpl implements ILimsTestItemService {
    private final LimsTestItemMapper baseMapper;
    @Override
    public TableDataInfo<LimsTestItem> queryPageList(LimsTestItem bo, PageQuery pageQuery) {
        Page<LimsTestItem> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }
    @Override
    public List<LimsTestItem> queryList(LimsTestItem bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }
    private LambdaQueryWrapper<LimsTestItem> buildQueryWrapper(LimsTestItem bo) {
        LambdaQueryWrapper<LimsTestItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ObjectUtil.isNotEmpty(bo.getInspectionId()), LimsTestItem::getInspectionId, bo.getInspectionId());
        lqw.like(ObjectUtil.isNotEmpty(bo.getItemName()), LimsTestItem::getItemName, bo.getItemName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), LimsTestItem::getStatus, bo.getStatus());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getTestGroupId()), LimsTestItem::getTestGroupId, bo.getTestGroupId());
        return lqw;
    }
    @Override public LimsTestItem queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(LimsTestItem bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(LimsTestItem bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
