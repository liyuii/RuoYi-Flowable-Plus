package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroupMember;

import java.util.Collection;
import java.util.List;

public interface ILimsTestGroupMemberService {

    TableDataInfo<LimsTestGroupMember> queryPageList(LimsTestGroupMember bo, PageQuery pageQuery);

    List<LimsTestGroupMember> queryList(LimsTestGroupMember bo);

    LimsTestGroupMember queryById(Long id);

    Boolean insert(LimsTestGroupMember bo);

    Boolean update(LimsTestGroupMember bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
