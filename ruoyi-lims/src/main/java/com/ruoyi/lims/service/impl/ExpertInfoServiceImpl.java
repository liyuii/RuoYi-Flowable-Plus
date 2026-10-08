package com.ruoyi.lims.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.lims.domain.ExpertInfo;
import com.ruoyi.lims.domain.ExpertMajor;
import com.ruoyi.lims.mapper.ExpertInfoMapper;
import com.ruoyi.lims.mapper.ExpertMajorMapper;
import com.ruoyi.lims.service.IExpertInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ExpertInfoServiceImpl implements IExpertInfoService {

    private final ExpertInfoMapper baseMapper;

    private final ExpertMajorMapper majorMapper;

    @Override
    public TableDataInfo<ExpertInfo> queryPageList(ExpertInfo bo, PageQuery pageQuery) {
        Page<ExpertInfo> page = pageQuery.build();
        IPage<ExpertInfo> result = baseMapper.selectExpertPage(page, bo);
        return TableDataInfo.build(result);
    }

    @Override
    public ExpertInfo queryById(Long id) {
        ExpertInfo info = baseMapper.selectById(id);
        if (info == null) {
            return null;
        }
        List<ExpertMajor> majors = majorMapper.selectList(
            new LambdaQueryWrapper<ExpertMajor>().eq(ExpertMajor::getExpertId, id));
        info.setMajorCodes(majors.stream().map(ExpertMajor::getMajorCode).collect(Collectors.toList()));
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(ExpertInfo bo) {
        validate(bo);
        // 身份证号是回避匹配的依据，同一个身份证号不允许重复建档
        Long exists = baseMapper.selectCount(new LambdaQueryWrapper<ExpertInfo>()
            .eq(ExpertInfo::getIdCard, bo.getIdCard()));
        if (exists != null && exists > 0) {
            throw new ServiceException("身份证号已存在，不能重复添加");
        }
        bo.setId(null);
        int rows = baseMapper.insert(bo);
        saveMajors(bo);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ExpertInfo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("专家ID不能为空");
        }
        validate(bo);
        int rows = baseMapper.updateById(bo);
        // 专业整体重建，简单且不会残留旧数据
        majorMapper.delete(new LambdaQueryWrapper<ExpertMajor>().eq(ExpertMajor::getExpertId, bo.getId()));
        saveMajors(bo);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        majorMapper.delete(new LambdaQueryWrapper<ExpertMajor>().in(ExpertMajor::getExpertId, ids));
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    private void saveMajors(ExpertInfo bo) {
        if (CollUtil.isEmpty(bo.getMajorCodes())) {
            return;
        }
        for (String majorCode : bo.getMajorCodes()) {
            if (StringUtils.isBlank(majorCode)) {
                continue;
            }
            ExpertMajor major = new ExpertMajor();
            major.setExpertId(bo.getId());
            major.setMajorCode(majorCode);
            majorMapper.insert(major);
        }
    }

    private void validate(ExpertInfo bo) {
        if (StringUtils.isBlank(bo.getExpertName())) {
            throw new ServiceException("专家姓名不能为空");
        }
        if (StringUtils.isBlank(bo.getIdCard())) {
            throw new ServiceException("身份证号不能为空");
        }
        if (StringUtils.isBlank(bo.getRegionCode())) {
            throw new ServiceException("所属地区不能为空");
        }
        if (CollUtil.isEmpty(bo.getMajorCodes())) {
            throw new ServiceException("至少要选一个专业");
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus("0");
        }
    }
}
