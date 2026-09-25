package com.ruoyi.doc.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文档详情视图对象：文档信息 + 页面图片列表
 */
@Data
public class DocDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private DocInfoVo doc;

    private List<DocPageVo> pages;

}
