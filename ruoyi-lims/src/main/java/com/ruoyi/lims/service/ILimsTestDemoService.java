package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestDemo;
import java.util.Collection;
import java.util.List;

public interface ILimsTestDemoService {
    TableDataInfo<LimsTestDemo> queryPageList(LimsTestDemo bo, PageQuery pageQuery);
    List<LimsTestDemo> queryList(LimsTestDemo bo);
    LimsTestDemo queryById(Long id);
    Boolean insert(LimsTestDemo bo);
    Boolean update(LimsTestDemo bo);
    Boolean deleteWithValidByIds(Collection<Long> ids);
}