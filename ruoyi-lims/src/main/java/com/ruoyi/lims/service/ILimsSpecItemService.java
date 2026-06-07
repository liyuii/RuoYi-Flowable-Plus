package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecItem;

import java.util.Collection;
import java.util.List;

public interface ILimsSpecItemService {

    TableDataInfo<LimsSpecItem> queryPageList(LimsSpecItem bo, PageQuery pageQuery);

    List<LimsSpecItem> queryList(LimsSpecItem bo);

    LimsSpecItem queryById(Long id);

    Boolean insert(LimsSpecItem bo);

    Boolean update(LimsSpecItem bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
