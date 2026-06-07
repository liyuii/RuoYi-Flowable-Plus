package com.ruoyi.lims.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecItem;
import com.ruoyi.lims.mapper.LimsSpecItemMapper;
import com.ruoyi.lims.service.ILimsSpecItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsSpecItemServiceImpl implements ILimsSpecItemService {
    private final LimsSpecItemMapper baseMapper;
    @Override
    public TableDataInfo<LimsSpecItem> queryPageList(LimsSpecItem bo, PageQuery pageQuery) {
        Page<LimsSpecItem> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }
    @Override
    public List<LimsSpecItem> queryList(LimsSpecItem bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }
    private LambdaQueryWrapper<LimsSpecItem> buildQueryWrapper(LimsSpecItem bo) {
        LambdaQueryWrapper<LimsSpecItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ObjectUtil.isNotEmpty(bo.getSpecId()), LimsSpecItem::getSpecId, bo.getSpecId());
        lqw.like(ObjectUtil.isNotEmpty(bo.getItemName()), LimsSpecItem::getItemName, bo.getItemName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getTestGroupId()), LimsSpecItem::getTestGroupId, bo.getTestGroupId());
        return lqw;
    }
    @Override public LimsSpecItem queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(LimsSpecItem bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(LimsSpecItem bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
