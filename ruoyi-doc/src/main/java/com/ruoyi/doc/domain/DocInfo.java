package com.ruoyi.doc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 文档信息对象 doc_info
 */
@Data
@TableName("doc_info")
public class DocInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目所在省份行政区划代码
     */
    private String provinceCode;

    /**
     * 项目金额（元）
     */
    private BigDecimal amount;

    /**
     * 来源业务类型：REVIEW_MASK 人工审核脱敏文件
     */
    private String sourceType;

    /**
     * 来源业务主键（review_doc.id）
     */
    private Long sourceBizId;

    /**
     * 文档文件 OSS 地址（脱敏后的 docx，下载用）
     */
    private String fileUrl;

    /**
     * 文档文件大小（字节）
     */
    private Long fileSize;

    /**
     * 页面图片数量
     */
    private Integer pageCount;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

    private Date createTime;

    private Date updateTime;

}
