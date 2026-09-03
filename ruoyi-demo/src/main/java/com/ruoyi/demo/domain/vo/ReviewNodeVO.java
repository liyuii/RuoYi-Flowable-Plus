package com.ruoyi.demo.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 文档节点视图对象：段落或表格
 */
@Data
public class ReviewNodeVO {

    /**
     * PARAGRAPH / TABLE
     */
    private String type;

    /**
     * 段落节点：块ID
     */
    private Long blockId;

    /**
     * 段落节点：文本
     */
    private String text;

    /**
     * 表格节点：行 -> 单元格
     */
    private List<List<ReviewCellVO>> rows;

}
