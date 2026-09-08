package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件管理上传文件对象 demo_upload_file
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
