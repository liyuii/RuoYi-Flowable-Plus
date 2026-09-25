package com.ruoyi.demo.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.config.ReviewAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 大模型调用客户端。
 * <p>
 * 走 OpenAI 兼容协议（/chat/completions），任何兼容该协议的厂商都可以直接换 base-url 使用。
 * 只负责发请求、取回回复正文；超时、报错、返回结构异常统一返回 null，
 * 由调用方降级处理，保证外部依赖不可用时主流程照常走通。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ReviewAiProperties properties;

    /**
     * 调用模型并返回回复正文，失败返回 null
     */
    public String chat(String systemPrompt, String userContent) {
        long start = System.currentTimeMillis();
        try {
            ObjectNode payload = MAPPER.createObjectNode();
            payload.put("model", properties.getModel());
            payload.put("temperature", 0);
            ArrayNode messages = payload.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userContent);

            try (HttpResponse response = HttpRequest.post(buildUrl())
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(properties.getTimeout())
                .body(payload.toString())
                .execute()) {
                String body = response.body();
                if (!response.isOk()) {
                    log.warn("AI 识别调用失败 status={} cost={}ms body={}",
                        response.getStatus(), System.currentTimeMillis() - start,
                        StringUtils.substring(body, 0, 500));
                    return null;
                }
                JsonNode root = MAPPER.readTree(body);
                JsonNode choices = root.path("choices");
                if (!choices.isArray() || choices.isEmpty()) {
                    log.warn("AI 识别返回结构异常 cost={}ms body={}",
                        System.currentTimeMillis() - start, StringUtils.substring(body, 0, 500));
                    return null;
                }
                String content = choices.get(0).path("message").path("content").asText(null);
                log.info("AI 识别调用完成 model={} 输入={}字 输出={}字 cost={}ms",
                    properties.getModel(), userContent.length(),
                    content == null ? 0 : content.length(), System.currentTimeMillis() - start);
                return content;
            }
        } catch (Exception e) {
            log.warn("AI 识别调用异常 cost={}ms", System.currentTimeMillis() - start, e);
            return null;
        }
    }

    private String buildUrl() {
        String base = properties.getBaseUrl().trim();
        return base.endsWith("/") ? base + "chat/completions" : base + "/chat/completions";
    }

}
