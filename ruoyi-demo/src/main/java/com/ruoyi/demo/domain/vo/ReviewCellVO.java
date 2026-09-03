package com.ruoyi.demo.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 表格单元格视图对象
 */
@Data
public class ReviewCellVO {

    private List<ReviewParagraphVO> paragraphs;

}
