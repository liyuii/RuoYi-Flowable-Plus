package com.ruoyi.lims.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestDemo;
import com.ruoyi.lims.mapper.LimsTestDemoMapper;
import com.ruoyi.lims.service.ILimsTestDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsTestDemoServiceImpl implements ILimsTestDemoService {

    private final LimsTestDemoMapper baseMapper;

    @Override
    public TableDataInfo<LimsTestDemo> queryPageList(LimsTestDemo bo, PageQuery pageQuery) {
        Page<LimsTestDemo> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<LimsTestDemo> queryList(LimsTestDemo bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<LimsTestDemo> buildQueryWrapper(LimsTestDemo bo) {
        LambdaQueryWrapper<LimsTestDemo> lqw = new LambdaQueryWrapper<>();
        lqw.like(ObjectUtil.isNotEmpty(bo.getDemoName()), LimsTestDemo::getDemoName, bo.getDemoName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getDemoType()), LimsTestDemo::getDemoType, bo.getDemoType());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), LimsTestDemo::getStatus, bo.getStatus());
        return lqw;
    }

    @Override public LimsTestDemo queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(LimsTestDemo bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(LimsTestDemo bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}