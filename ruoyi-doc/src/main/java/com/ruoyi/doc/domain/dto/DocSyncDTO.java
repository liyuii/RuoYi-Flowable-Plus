package com.ruoyi.doc.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 文档同步 DTO：源系统（投标方案库）与文档库之间的唯一传输对象，
 * 文档库不依赖源系统的实体，将来改成 HTTP 调用时也是传这份数据。
 */
@Data
public class DocSyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来源业务类型：REVIEW_MASK 人工审核脱敏文件
     */
    private String sourceType;

    /**
     * 来源业务主键（review_doc.id）
     */
    private Long sourceBizId;

    private String docName;

    private String projectName;

    private String provinceCode;

    private BigDecimal amount;

    /**
     * 文档文件（脱敏后的 docx）OSS 地址，下载用
     */
    private String fileUrl;

    private Long fileSize;

    /**
     * 页面图片清单，页码从 1 开始
     */
    private List<DocPageDTO> pages;

}
