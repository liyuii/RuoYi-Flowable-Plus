package com.ruoyi.lims.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroup;
import com.ruoyi.lims.mapper.LimsTestGroupMapper;
import com.ruoyi.lims.service.ILimsTestGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsTestGroupServiceImpl implements ILimsTestGroupService {

    private final LimsTestGroupMapper baseMapper;

    @Override
    public TableDataInfo<LimsTestGroup> queryPageList(LimsTestGroup bo, PageQuery pageQuery) {
        LambdaQueryWrapper<LimsTestGroup> lqw = buildQueryWrapper(bo);
        Page<LimsTestGroup> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public List<LimsTestGroup> queryList(LimsTestGroup bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<LimsTestGroup> buildQueryWrapper(LimsTestGroup bo) {
        LambdaQueryWrapper<LimsTestGroup> lqw = new LambdaQueryWrapper<>();
        lqw.like(ObjectUtil.isNotEmpty(bo.getGroupName()), LimsTestGroup::getGroupName, bo.getGroupName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getGroupCode()), LimsTestGroup::getGroupCode, bo.getGroupCode());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), LimsTestGroup::getStatus, bo.getStatus());
        return lqw;
    }

    @Override
    public LimsTestGroup queryById(Long id) { return baseMapper.selectById(id); }

    @Override
    public Boolean insert(LimsTestGroup bo) { return baseMapper.insert(bo) > 0; }

    @Override
    public Boolean update(LimsTestGroup bo) { return baseMapper.updateById(bo) > 0; }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
