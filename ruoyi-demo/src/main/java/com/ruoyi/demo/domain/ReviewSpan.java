package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 敏感识别记录对象 review_span
 */
@Data
@TableName("review_span")
public class ReviewSpan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID，关联 review_doc.id
     */
    private Long docId;

    /**
     * 文本块ID，按文档解析顺序生成
     */
    private Long blockId;

    /**
     * 块内起始偏移，从0开始，含
     */
    private Integer startChar;

    /**
     * 块内结束偏移，从0开始，不含
     */
    private Integer endChar;

    /**
     * 识别出的原文片段
     */
    private String spanText;

    /**
     * 类型：人名/地名/单位/电话/其他
     */
    private String spanType;

    /**
     * 来源：M 机器识别，A 人工补充
     */
    private String source;

    /**
     * 状态：0 待确认，1 已确认，2 已忽略
     */
    private String status;

    private Date createTime;

    private Date updateTime;

}
