package com.ruoyi.doc.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 页面图片同步 DTO
 */
@Data
public class DocPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNo;

    private String fileUrl;

    private Long fileSize;

    private Integer width;

    private Integer height;

}
