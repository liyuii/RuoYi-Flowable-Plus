package com.ruoyi.doc.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文档页面图片视图对象
 */
@Data
public class DocPageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNo;

    /**
     * 图片访问地址（私有桶返回签名地址，前端直接给 img src 用）
     */
    private String imageUrl;

    /**
     * 图片 OSS 原始地址
     */
    private String fileUrl;

    private Long fileSize;

    private Integer width;

    private Integer height;

}
