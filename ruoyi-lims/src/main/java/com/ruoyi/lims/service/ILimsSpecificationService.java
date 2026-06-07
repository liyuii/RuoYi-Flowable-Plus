package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecification;

import java.util.Collection;
import java.util.List;

public interface ILimsSpecificationService {

    TableDataInfo<LimsSpecification> queryPageList(LimsSpecification bo, PageQuery pageQuery);

    List<LimsSpecification> queryList(LimsSpecification bo);

    LimsSpecification queryById(Long id);

    Boolean insert(LimsSpecification bo);

    Boolean update(LimsSpecification bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
