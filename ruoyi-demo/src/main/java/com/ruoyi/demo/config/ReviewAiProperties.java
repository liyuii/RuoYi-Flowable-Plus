package com.ruoyi.demo.config;

import com.ruoyi.common.utils.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 投标方案库 - AI 敏感词识别配置
 * <p>
 * 演示用 OpenAI 兼容协议调用云端模型；未配置密钥或开关关闭时自动降级为只用正则引擎。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ruoyi.review.ai")
public class ReviewAiProperties {

    /**
     * 占位符前缀：配置里预留了 key 但还没替换成真实值时，视为未配置，避免无意义的失败调用
     */
    private static final String PLACEHOLDER_PREFIX = "sk-REPLACE";

    /**
     * 是否启用 AI 识别
     */
    private boolean enabled = false;

    /**
     * OpenAI 兼容接口地址，例如 https://api.deepseek.com/v1
     */
    private String baseUrl;

    /**
     * 接口密钥，保留占位符时视为未配置
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 单次请求超时，单位毫秒
     */
    private int timeout = 60000;

    /**
     * 单批文本字数上限，按段落攒批，避免单次请求过长
     */
    private int batchSize = 3000;

    /**
     * AI 是否可用：开关打开且地址、密钥、模型都已配置
     */
    public boolean available() {
        return enabled
            && StringUtils.isNotBlank(baseUrl)
            && StringUtils.isNotBlank(apiKey)
            && !apiKey.trim().startsWith(PLACEHOLDER_PREFIX)
            && StringUtils.isNotBlank(model);
    }

}
