package com.ruoyi.demo.service;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 敏感词识别提示词提供者。
 * <p>
 * 优先读取 sys_config（系统管理 - 参数设置里可维护），读不到时回落到代码中的默认提示词。
 * 注意：提示词可以配置，但返回结果的解析与校验固定在代码里（见 ReviewSensitiveRecognizer），
 * 避免改了文案导致返回结构变化后程序解析失败。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewPromptProvider {

    /**
     * sys_config 中的参数键名
     */
    public static final String CONFIG_KEY = "review.ai.prompt.recognize";

    /**
     * 默认提示词（代码兜底版本），与 sys_config 中的内容保持一致
     */
    public static final String DEFAULT_RECOGNIZE_PROMPT =
        "你是招投标文件脱敏助手，从文本中找出需要人工确认的敏感信息。\n"
            + "\n"
            + "类型 type 只能取其一：人名、地名、机构名、项目名、电话、日期、数量、其他。\n"
            + "- 人名：法定代表人、项目经理、联系人等真实姓名\n"
            + "- 地名：省市区县、街道、具体地址\n"
            + "- 机构名：公司、集团、研究院、医院、监理单位等完整名称\n"
            + "- 项目名：工程或项目全称\n"
            + "- 日期：如 2026年9月、2026-09-23\n"
            + "- 数量：面积、规模、金额、工期等数字，如 86000平方米、540日历天\n"
            + "- 其他：证书编号、账号等\n"
            + "\n"
            + "只输出 JSON，不要解释、不要代码块：\n"
            + "{\"spans\":[{\"text\":\"原文片段\",\"type\":\"机构名\"}]}\n"
            + "\n"
            + "要求：\n"
            + "1. text 必须与原文逐字一致，不得改写、缩写、补全；\n"
            + "2. 手机号、身份证号、统一社会信用代码不必输出，由系统规则处理；\n"
            + "3. 不要输出“投标文件”“施工方案”等通用词；\n"
            + "4. 同一个词只输出一次；无敏感信息时输出 {\"spans\":[]}。";

    private final ISysConfigService configService;

    /**
     * 获取识别提示词及其来源
     */
    public Prompt getRecognizePrompt() {
        try {
            String value = configService.selectConfigByKey(CONFIG_KEY);
            if (StringUtils.isNotBlank(value)) {
                return new Prompt(value, "sys_config");
            }
        } catch (Exception e) {
            log.warn("读取提示词配置失败，改用默认提示词 configKey={}", CONFIG_KEY, e);
        }
        return new Prompt(DEFAULT_RECOGNIZE_PROMPT, "default");
    }

    /**
     * 提示词内容与来源
     */
    @Getter
    @RequiredArgsConstructor
    public static class Prompt {

        /**
         * 提示词内容
         */
        private final String content;

        /**
         * 来源：sys_config / default
         */
        private final String source;
    }

}
