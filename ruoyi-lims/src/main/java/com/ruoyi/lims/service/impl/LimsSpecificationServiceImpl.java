package com.ruoyi.lims.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecification;
import com.ruoyi.lims.mapper.LimsSpecificationMapper;
import com.ruoyi.lims.service.ILimsSpecificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsSpecificationServiceImpl implements ILimsSpecificationService {
    private final LimsSpecificationMapper baseMapper;
    @Override
    public TableDataInfo<LimsSpecification> queryPageList(LimsSpecification bo, PageQuery pageQuery) {
        Page<LimsSpecification> page = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }
    @Override
    public List<LimsSpecification> queryList(LimsSpecification bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }
    private LambdaQueryWrapper<LimsSpecification> buildQueryWrapper(LimsSpecification bo) {
        LambdaQueryWrapper<LimsSpecification> lqw = new LambdaQueryWrapper<>();
        lqw.like(ObjectUtil.isNotEmpty(bo.getSpecName()), LimsSpecification::getSpecName, bo.getSpecName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getMaterialName()), LimsSpecification::getMaterialName, bo.getMaterialName());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getStatus()), LimsSpecification::getStatus, bo.getStatus());
        return lqw;
    }
    @Override public LimsSpecification queryById(Long id) { return baseMapper.selectById(id); }
    @Override public Boolean insert(LimsSpecification bo) { return baseMapper.insert(bo) > 0; }
    @Override public Boolean update(LimsSpecification bo) { return baseMapper.updateById(bo) > 0; }
    @Override public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
