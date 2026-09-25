package com.ruoyi.doc.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.doc.domain.bo.DocInfoBo;
import com.ruoyi.doc.domain.vo.DocDetailVo;
import com.ruoyi.doc.domain.vo.DocInfoVo;

import javax.servlet.http.HttpServletResponse;

/**
 * 文档管理Service接口
 */
public interface IDocInfoService {

    /**
     * 分页查询文档列表
     */
    TableDataInfo<DocInfoVo> pageList(DocInfoBo bo, PageQuery pageQuery);

    /**
     * 文档详情（文档信息 + 页面图片列表）
     */
    DocDetailVo detail(Long id);

    /**
     * 下载文档文件（整个 docx，图片只用于预览）
     */
    void download(Long id, HttpServletResponse response);

    /**
     * 删除文档（逻辑删除主表 + 删除页图片明细；OSS 对象归属源系统，不在这里删）
     */
    void remove(Long id);

}
