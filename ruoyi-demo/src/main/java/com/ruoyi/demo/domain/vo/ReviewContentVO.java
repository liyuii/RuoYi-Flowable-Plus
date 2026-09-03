package com.ruoyi.demo.domain.vo;

import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import lombok.Data;

import java.util.List;

/**
 * 审核页数据对象
 */
@Data
public class ReviewContentVO {

    private ReviewDoc doc;

    private List<ReviewNodeVO> nodes;

    private List<ReviewSpan> spans;

}
