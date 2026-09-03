package com.ruoyi.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 人工审核文档对象 review_doc
 */
@Data
@TableName("review_doc")
public class ReviewDoc {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 本地 docx 文件路径
     */
    private String filePath;

    /**
     * 状态：0 待审核，1 已审核
     */
    private String status;

    private Date createTime;

    private Date updateTime;

}
