package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsInspection;

import java.util.Collection;
import java.util.List;

public interface ILimsInspectionService {

    TableDataInfo<LimsInspection> queryPageList(LimsInspection bo, PageQuery pageQuery);

    List<LimsInspection> queryList(LimsInspection bo);

    LimsInspection queryById(Long id);

    Boolean insert(LimsInspection bo);

    Boolean update(LimsInspection bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
