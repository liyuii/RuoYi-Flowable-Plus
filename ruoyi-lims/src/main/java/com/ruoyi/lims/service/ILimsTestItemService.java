package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestItem;

import java.util.Collection;
import java.util.List;

public interface ILimsTestItemService {

    TableDataInfo<LimsTestItem> queryPageList(LimsTestItem bo, PageQuery pageQuery);

    List<LimsTestItem> queryList(LimsTestItem bo);

    LimsTestItem queryById(Long id);

    Boolean insert(LimsTestItem bo);

    Boolean update(LimsTestItem bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
