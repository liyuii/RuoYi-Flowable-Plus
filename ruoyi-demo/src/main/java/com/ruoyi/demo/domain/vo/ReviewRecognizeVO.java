package com.ruoyi.demo.domain.vo;

import lombok.Data;

/**
 * 脱敏识别结果视图对象
 */
@Data
public class ReviewRecognizeVO {

    /**
     * 去重后的候选敏感词数量
     */
    private Integer candidateCount;

    /**
     * 本次新增入库数量，已存在的词会被跳过
     */
    private Integer newCount;

    /**
     * 候选词中来自 AI 的数量
     */
    private Integer aiCount;

    /**
     * 候选词中来自正则的数量
     */
    private Integer regexCount;

    /**
     * AI 是否真正参与本次识别
     */
    private Boolean aiUsed;

    /**
     * 提示词来源：sys_config / default
     */
    private String promptSource;

    /**
     * 耗时，单位毫秒
     */
    private Long costMs;

}
