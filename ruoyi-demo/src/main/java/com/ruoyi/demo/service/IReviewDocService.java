package com.ruoyi.demo.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;

import javax.servlet.http.HttpServletResponse;

/**
 * 人工审核Service接口
 */
public interface IReviewDocService {

    /**
     * 分页查询投标文件
     */
    TableDataInfo<ReviewDoc> pageDocs(ReviewDoc query, PageQuery pageQuery);

    /**
     * 查询投标文件详情
     */
    ReviewDoc getDoc(Long docId);

    /**
     * 新增投标文件（原文件已上传到 OSS）
     */
    void createDoc(ReviewDoc doc);

    /**
     * 修改投标文件基础信息
     */
    void updateDoc(ReviewDoc doc);

    /**
     * 逻辑删除投标文件
     */
    void deleteDoc(Long docId);

    /**
     * 拆分章节：提取标题命中关键字的章节，生成本地截取文件
     */
    ReviewDoc split(Long docId);

    /**
     * 下载截取文件
     */
    void downloadExtract(Long docId, HttpServletResponse response);

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
