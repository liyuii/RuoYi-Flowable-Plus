package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 文件管理上传文件对象 demo_upload_file
 * <p>
 * 用户文件管理里存的是用户上传的投标方案模板：文件先落本地，
 * 审核通过后再上传 OSS 并发布到方案库（review_doc，source_type = USER_UPLOAD）。
 */
@Data
@TableName("demo_upload_file")
public class DemoUploadFile {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传会话唯一编号
     */
    private String uploadId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目金额（元）
     */
    private BigDecimal amount;

    /**
     * 项目所在省份行政区划代码
     */
    private String provinceCode;

    /**
     * 文件总大小，字节
     */
    private Long fileSize;

    /**
     * 整个文件 SHA-256
     */
    private String fileHash;

    /**
     * 分片大小，字节
     */
    private Integer chunkSize;

    /**
     * 总片数
     */
    private Integer totalChunks;

    /**
     * 状态：0 上传中，1 已完成，2 已取消
     */
    private Integer status;

    /**
     * 审核状态：0 待审核，1 审核通过，2 审核不通过
     */
    private String auditStatus;

    /**
     * 审核不通过原因
     */
    private String auditReason;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核通过后发布到文档库（投标方案列表）的记录ID，对应 doc_info.id
     */
    private Long docInfoId;

    /**
     * 审核通过后上传到 OSS 的文件ID，对应 sys_file.id（用于删除时连带清理 OSS 对象）
     */
    private Long publishFileId;

    /**
     * 上传人（列表按它做数据隔离）
     */
    private String createBy;

    /**
     * 合并后的正式文件路径
     */
    private String storagePath;

    /**
     * 逻辑删除：0 否，1 是
     */
    private Integer deleted;

    private Date createTime;

    private Date updateTime;

}
