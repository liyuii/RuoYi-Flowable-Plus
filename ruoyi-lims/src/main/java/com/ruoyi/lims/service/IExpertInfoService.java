package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ExpertInfo;

import java.util.Collection;

/**
 * 评标专家 Service
 */
public interface IExpertInfoService {

    TableDataInfo<ExpertInfo> queryPageList(ExpertInfo bo, PageQuery pageQuery);

    ExpertInfo queryById(Long id);

    Boolean insert(ExpertInfo bo);

    Boolean update(ExpertInfo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
