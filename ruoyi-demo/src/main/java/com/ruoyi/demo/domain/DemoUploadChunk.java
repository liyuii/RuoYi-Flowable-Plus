package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件管理分片记录对象 demo_upload_chunk
 */
@Data
@TableName("demo_upload_chunk")
public class DemoUploadChunk {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传会话编号
     */
    private String uploadId;

    /**
     * 分片序号，从 0 开始
     */
    private Integer chunkIndex;

    /**
     * 该分片实际字节数
     */
    private Long chunkSize;

    /**
     * 状态：1 已上传
     */
    private Integer status;

    private Date createTime;

    private Date updateTime;

}
