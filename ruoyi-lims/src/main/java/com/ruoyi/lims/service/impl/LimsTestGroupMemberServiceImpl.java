package com.ruoyi.lims.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroupMember;
import com.ruoyi.lims.mapper.LimsTestGroupMemberMapper;
import com.ruoyi.lims.service.ILimsTestGroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LimsTestGroupMemberServiceImpl implements ILimsTestGroupMemberService {

    private final LimsTestGroupMemberMapper baseMapper;

    @Override
    public TableDataInfo<LimsTestGroupMember> queryPageList(LimsTestGroupMember bo, PageQuery pageQuery) {
        LambdaQueryWrapper<LimsTestGroupMember> lqw = buildQueryWrapper(bo);
        Page<LimsTestGroupMember> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public List<LimsTestGroupMember> queryList(LimsTestGroupMember bo) {
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<LimsTestGroupMember> buildQueryWrapper(LimsTestGroupMember bo) {
        LambdaQueryWrapper<LimsTestGroupMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ObjectUtil.isNotEmpty(bo.getGroupId()), LimsTestGroupMember::getGroupId, bo.getGroupId());
        lqw.eq(ObjectUtil.isNotEmpty(bo.getUserId()), LimsTestGroupMember::getUserId, bo.getUserId());
        return lqw;
    }

    @Override
    public LimsTestGroupMember queryById(Long id) { return baseMapper.selectById(id); }

    @Override
    public Boolean insert(LimsTestGroupMember bo) { return baseMapper.insert(bo) > 0; }

    @Override
    public Boolean update(LimsTestGroupMember bo) { return baseMapper.updateById(bo) > 0; }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) { return baseMapper.deleteBatchIds(ids) > 0; }
}
