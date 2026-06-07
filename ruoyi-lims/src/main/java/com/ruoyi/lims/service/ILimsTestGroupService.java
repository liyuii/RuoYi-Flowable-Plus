package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroup;

import java.util.Collection;
import java.util.List;

public interface ILimsTestGroupService {

    TableDataInfo<LimsTestGroup> queryPageList(LimsTestGroup bo, PageQuery pageQuery);

    List<LimsTestGroup> queryList(LimsTestGroup bo);

    LimsTestGroup queryById(Long id);

    Boolean insert(LimsTestGroup bo);

    Boolean update(LimsTestGroup bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
