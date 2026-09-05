package com.ruoyi.demo.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;

/**
 * 人工审核Service接口
 */
public interface IReviewDocService {

    TableDataInfo<ReviewDoc> pageDocs(PageQuery pageQuery);

    ReviewContentVO content(Long docId);

    Boolean confirmSpan(Long id);

    Boolean ignoreSpan(Long id);

    ReviewSpan addSpan(ReviewSpan span);

    ReviewSpan updateSpan(ReviewSpan span);

    /**
     * 审核完成：校验没有待确认记录后，把文档状态改为 2
     */
    Boolean completeReview(Long docId);

    String applyMask(Long docId);

}
