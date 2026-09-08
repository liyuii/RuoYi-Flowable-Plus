package com.ruoyi.demo.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 上传会话信息
 */
@Data
public class DemoUploadSessionVo {

    /**
     * 上传会话编号
     */
    private String uploadId;

    /**
     * 分片大小，字节
     */
    private Integer chunkSize;

    /**
     * 总片数
     */
    private Integer totalChunks;

    /**
     * 已上传分片序号
     */
    private List<Integer> uploadedChunkIndexes;

}
