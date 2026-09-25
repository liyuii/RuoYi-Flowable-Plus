package com.ruoyi.doc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文档文件对象 doc_file（页面图片，只用于详情页预览）
 */
@Data
@TableName("doc_file")
public class DocFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档信息ID，关联 doc_info.id
     */
    private Long docId;

    /**
     * 文件类型：PAGE 页图片
     */
    private String fileType;

    /**
     * 页码，从1开始
     */
    private Integer pageNo;

    /**
     * 图片 OSS 地址
     */
    private String fileUrl;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private Date createTime;

}
