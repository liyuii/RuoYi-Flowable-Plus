package com.ruoyi.doc.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 文档列表视图对象
 */
@Data
public class DocInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String docName;

    private String projectName;

    private String provinceCode;

    private BigDecimal amount;

    private String sourceType;

    private Long sourceBizId;

    /**
     * 文档文件 OSS 地址（原始地址，下载走后端接口）
     */
    private String fileUrl;

    private Long fileSize;

    private Integer pageCount;

    private Date createTime;

    /**
     * 首页图片访问地址（列表缩略图，私有桶返回签名地址）
     */
    private String coverUrl;

}
