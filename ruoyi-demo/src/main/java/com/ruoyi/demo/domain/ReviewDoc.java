package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 投标方案文档对象 review_doc
 */
@Data
@TableName("review_doc")
public class ReviewDoc {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档名称（取上传文件的原始名）
     */
    private String docName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目金额（元）
     */
    private BigDecimal amount;

    /**
     * 项目所在省份行政区划代码，如 130000
     */
    private String provinceCode;

    /**
     * 当前处理文件路径：新增时存原始文件地址，拆分后为本地工作副本路径
     */
    private String filePath;

    /**
     * 原始文件在 sys_file 表中的 id
     */
    private Long originalFileId;

    /**
     * 审核状态：0 待审核，1 审核中，2 审核完成
     */
    private String status;

    /**
     * 提取出的方案章节文件（本地工作目录）
     */
    private String extractFilePath;

    /**
     * 脱敏后文件在 OSS 的地址，审核完成时自动生成（本地工作目录另留 masked.docx 副本）
     */
    private String maskFilePath;

    /**
     * 脱敏文件在 sys_file 表中的 id，用于重新脱敏时删除上一版 OSS 对象
     */
    private Long maskFileId;

    /**
     * 本次提取到的章节标题，多个用分号分隔
     */
    private String extractChapters;

    /**
     * 流程状态：WAIT_SPLIT 待拆分，SPLIT_DONE 已拆分，
     * WAIT_REVIEW 待审核，WAIT_MASK 待脱敏（审核完成但脱敏失败），MASK_DONE 脱敏完成
     */
    private String processStatus;

    /**
     * 最近一次处理失败原因，成功后清空
     */
    private String processError;

    /**
     * 处理重试次数
     */
    private Integer retryCount;

    /**
     * 删除标志（0 代表存在，2 代表删除）
     */
    @TableLogic
    private String delFlag;

    private Date createTime;

    private Date updateTime;

}
