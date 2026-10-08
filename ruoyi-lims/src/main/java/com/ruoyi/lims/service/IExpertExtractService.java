package com.ruoyi.lims.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ExpertExtract;
import com.ruoyi.lims.domain.bo.ExpertExtractAuditBo;
import com.ruoyi.lims.domain.bo.ExpertExtractBo;
import com.ruoyi.lims.domain.bo.ExpertExtractResultBo;
import com.ruoyi.lims.domain.bo.ExpertMajorCountBo;
import com.ruoyi.lims.domain.vo.ExpertDrawCheckVo;
import com.ruoyi.lims.domain.vo.ExpertDrawResultVo;
import com.ruoyi.lims.domain.vo.ExpertExtractDetailVo;
import com.ruoyi.lims.domain.vo.ExpertMajorCountVo;

import java.util.Collection;
import java.util.List;

/**
 * 专家抽取申请 Service
 */
public interface IExpertExtractService {

    TableDataInfo<ExpertExtract> queryPageList(ExpertExtract bo, PageQuery pageQuery);

    ExpertExtractDetailVo queryDetail(Long id);

    Boolean insert(ExpertExtractBo bo);

    Boolean update(ExpertExtractBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    /** 提交审核 */
    Boolean submit(Long id);

    /** 审核 */
    Boolean audit(ExpertExtractAuditBo bo);

    /** 预检：只看每条范围够不够，不落库 */
    List<ExpertDrawCheckVo> check(Long id);

    /** 统计各专业当前可抽取的人数，给选择专业的下拉用 */
    List<ExpertMajorCountVo> queryMajorCount(ExpertMajorCountBo bo);

    /** 首次抽取 */
    ExpertDrawResultVo draw(Long id);

    /** 补充抽取 */
    ExpertDrawResultVo supplement(Long id);

    /** 变更某位专家的参加状态 */
    Boolean updateResultStatus(ExpertExtractResultBo bo);
}
