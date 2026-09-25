package com.ruoyi.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.config.ReviewAiProperties;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewCellVO;
import com.ruoyi.demo.domain.vo.ReviewNodeVO;
import com.ruoyi.demo.domain.vo.ReviewParagraphVO;
import com.ruoyi.demo.domain.vo.ReviewRecognizeVO;
import com.ruoyi.demo.mapper.ReviewSpanMapper;
import com.ruoyi.demo.utils.ReviewDocxParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 敏感词识别服务。
// 正则引擎与 AI 引擎并联：正则负责手机号、统一社会信用代码、金额等强模式；
// AI 负责人名、地名、机构名、项目名等需要上下文判断的泛化识别。
// 两者结果合并去重后写入 review_span，等待人工审核确认，识别结果不直接用于替换。
// AI 不可用（未配置、超时、返回异常）时自动跳过，只用正则结果，主流程不受影响。
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSensitiveRecognizer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SOURCE_REGEX = "REGEX";

    private static final String SOURCE_AI = "AI";

    private static final Set<String> ALLOWED_TYPES = new LinkedHashSet<>(
        Arrays.asList("人名", "地名", "机构名", "项目名", "电话", "日期", "数量", "其他"));

    private static final Map<String, Integer> TYPE_PRIORITY = new HashMap<>();

    static {
        TYPE_PRIORITY.put("电话", 60);
        TYPE_PRIORITY.put("机构名", 50);
        TYPE_PRIORITY.put("项目名", 40);
        TYPE_PRIORITY.put("人名", 30);
        TYPE_PRIORITY.put("地名", 20);
        TYPE_PRIORITY.put("日期", 18);
        TYPE_PRIORITY.put("数量", 15);
        TYPE_PRIORITY.put("其他", 10);
    }

    // 正则规则：数字类强模式交给正则（确定性高），AI 只负责需要上下文判断的实体；
    // 身份证/邮箱按方案第一版暂不纳入，避免误报
    private static final List<RegexRule> REGEX_RULES = Arrays.asList(
        new RegexRule("电话", Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)")),
        new RegexRule("其他", Pattern.compile("(?<![0-9A-Z])[0-9A-HJ-NPQRTUWXY]{18}(?![0-9A-Z])")),
        new RegexRule("数量", Pattern.compile("\\d+(?:,\\d{3})*(?:\\.\\d+)?\\s*(?:万元|亿元|元)")),
        new RegexRule("数量", Pattern.compile("\\d+(?:\\.\\d+)?\\s*(?:万平方米|平方米|平米|㎡)")),
        new RegexRule("日期", Pattern.compile("\\d{4}\\s*年\\s*\\d{1,2}\\s*月(?:\\s*\\d{1,2}\\s*日)?")),
        new RegexRule("日期", Pattern.compile("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}"))
    );

    private final ReviewSpanMapper spanMapper;

    private final ReviewAiClient aiClient;

    private final ReviewPromptProvider promptProvider;

    private final ReviewAiProperties aiProperties;

    // 对截取文件执行敏感词识别，候选词写入 review_span（status=0 待确认）
    public ReviewRecognizeVO recognize(ReviewDoc doc) {
        long start = System.currentTimeMillis();
        List<String> paragraphs = paragraphTexts(doc.getExtractFilePath());
        if (paragraphs.isEmpty()) {
            throw new ServiceException("截取文件中没有可识别的文本内容");
        }

        Map<String, Candidate> candidates = new LinkedHashMap<>();
        collectByRegex(paragraphs, candidates);

        ReviewPromptProvider.Prompt prompt = promptProvider.getRecognizePrompt();
        AiResult aiResult = new AiResult();
        if (aiProperties.available()) {
            aiResult = collectByAi(doc.getId(), paragraphs, prompt.getContent(), candidates);
        } else {
            log.info("AI 识别已跳过（未启用或未配置 api-key），本次只用正则引擎 docId={}", doc.getId());
        }

        Set<String> existing = spanMapper.selectList(Wrappers.<ReviewSpan>lambdaQuery()
                .eq(ReviewSpan::getDocId, doc.getId()))
            .stream()
            .map(ReviewSpan::getSpanText)
            .collect(Collectors.toSet());

        int newCount = 0;
        Date now = new Date();
        for (Candidate candidate : candidates.values()) {
            if (existing.contains(candidate.text)) {
                continue;
            }
            ReviewSpan span = new ReviewSpan();
            span.setDocId(doc.getId());
            span.setSpanText(candidate.text);
            span.setSpanType(candidate.type);
            span.setSource(candidate.source);
            span.setStatus("0");
            span.setCreateTime(now);
            span.setUpdateTime(now);
            spanMapper.insert(span);
            newCount++;
        }

        int regexCount = (int) candidates.values().stream()
            .filter(item -> SOURCE_REGEX.equals(item.source))
            .count();
        int aiCount = (int) candidates.values().stream()
            .filter(item -> SOURCE_AI.equals(item.source))
            .count();

        ReviewRecognizeVO vo = new ReviewRecognizeVO();
        vo.setCandidateCount(candidates.size());
        vo.setNewCount(newCount);
        vo.setAiCount(aiCount);
        vo.setRegexCount(regexCount);
        vo.setAiUsed(aiResult.totalBatch > 0 && aiResult.successBatch > 0);
        vo.setPromptSource(prompt.getSource());
        vo.setCostMs(System.currentTimeMillis() - start);
        log.info("敏感词识别完成 docId={} 段落数={} 候选={} 新增={} AI={} 正则={} ai批次={}/{} promptSource={} cost={}ms",
            doc.getId(), paragraphs.size(), vo.getCandidateCount(), newCount, aiCount, regexCount,
            aiResult.successBatch, aiResult.totalBatch, prompt.getSource(), vo.getCostMs());
        return vo;
    }

    // 按文档阅读顺序取全部段落文本（正文段落 + 表格单元格内段落）
    private List<String> paragraphTexts(String filePath) {
        List<String> texts = new ArrayList<>();
        try {
            for (ReviewNodeVO node : ReviewDocxParser.parse(filePath)) {
                if ("PARAGRAPH".equals(node.getType())) {
                    addText(texts, node.getText());
                } else if ("TABLE".equals(node.getType()) && node.getRows() != null) {
                    for (List<ReviewCellVO> row : node.getRows()) {
                        for (ReviewCellVO cell : row) {
                            if (cell.getParagraphs() == null) {
                                continue;
                            }
                            for (ReviewParagraphVO paragraph : cell.getParagraphs()) {
                                addText(texts, paragraph.getText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("解析截取文件失败: " + e.getMessage());
        }
        return texts;
    }

    private void addText(List<String> texts, String text) {
        if (StringUtils.isNotBlank(text)) {
            texts.add(text.trim());
        }
    }

    // 正则引擎：命中手机号、统一社会信用代码、金额
    private void collectByRegex(List<String> paragraphs, Map<String, Candidate> candidates) {
        for (String paragraph : paragraphs) {
            for (RegexRule rule : REGEX_RULES) {
                Matcher matcher = rule.pattern.matcher(paragraph);
                while (matcher.find()) {
                    merge(candidates, new Candidate(matcher.group().trim(), rule.type, SOURCE_REGEX));
                }
            }
        }
    }

    // AI 引擎：按字数攒批调用模型，返回结果逐条校验后并入候选集合
    private AiResult collectByAi(Long docId, List<String> paragraphs, String systemPrompt,
                                 Map<String, Candidate> candidates) {
        List<String> batches = batch(paragraphs, aiProperties.getBatchSize());
        AiResult result = new AiResult();
        result.totalBatch = batches.size();
        for (int i = 0; i < batches.size(); i++) {
            String batchText = batches.get(i);
            String content = aiClient.chat(systemPrompt, batchText);
            if (StringUtils.isBlank(content)) {
                log.warn("AI 识别批次无有效返回 docId={} batch={}/{}", docId, i + 1, batches.size());
                continue;
            }
            result.successBatch++;
            log.info("AI 原始返回 docId={} batch={}/{} content={}",
                docId, i + 1, batches.size(), StringUtils.substring(content, 0, 800));
            List<Candidate> parsed = parseCandidates(docId, i + 1, content, batchText);
            result.candidateCount += parsed.size();
            for (Candidate candidate : parsed) {
                merge(candidates, candidate);
            }
        }
        return result;
    }

    private List<String> batch(List<String> paragraphs, int batchSize) {
        int limit = batchSize > 0 ? batchSize : 3000;
        List<String> batches = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (buffer.length() > 0 && buffer.length() + paragraph.length() > limit) {
                batches.add(buffer.toString());
                buffer.setLength(0);
            }
            buffer.append(paragraph).append('\n');
        }
        if (buffer.length() > 0) {
            batches.add(buffer.toString());
        }
        return batches;
    }

    // 解析 AI 返回内容：剥掉可能的代码块围栏、校验词必须出现在原文中、类型归一化
    private List<Candidate> parseCandidates(Long docId, int batchNo, String content, String batchText) {
        List<Candidate> list = new ArrayList<>();
        String json = extractJson(content);
        if (json == null) {
            log.warn("AI 返回内容无法解析为 JSON，已丢弃 docId={} batch={} content={}",
                docId, batchNo, StringUtils.substring(content, 0, 300));
            return list;
        }
        try {
            JsonNode spans = MAPPER.readTree(json).path("spans");
            if (!spans.isArray()) {
                return list;
            }
            for (JsonNode node : spans) {
                String text = node.path("text").asText("").trim();
                if (text.length() < 2) {
                    continue;
                }
                if (!batchText.contains(text)) {
                    log.warn("AI 返回的词未在原文中出现，已丢弃 docId={} batch={} word={}",
                        docId, batchNo, StringUtils.substring(text, 0, 50));
                    continue;
                }
                list.add(new Candidate(text, normalizeType(node.path("type").asText("")), SOURCE_AI));
            }
        } catch (Exception e) {
            log.warn("AI 返回内容解析失败，已丢弃 docId={} batch={} content={}",
                docId, batchNo, StringUtils.substring(content, 0, 300), e);
        }
        return list;
    }

    private String extractJson(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*\\s*", "");
            text = text.replaceFirst("```\\s*$", "");
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    // 类型归一化：模型偶尔返回“企业”“组织”这类同义类型，统一映射到白名单值
    private String normalizeType(String rawType) {
        String type = rawType == null ? "" : rawType.trim();
        if (ALLOWED_TYPES.contains(type)) {
            return type;
        }
        if (type.contains("人名") || type.contains("姓名")) {
            return "人名";
        }
        if (type.contains("地名") || type.contains("地址")) {
            return "地名";
        }
        if (type.contains("机构") || type.contains("公司") || type.contains("单位") || type.contains("组织")) {
            return "机构名";
        }
        if (type.contains("项目") || type.contains("工程")) {
            return "项目名";
        }
        if (type.contains("电话") || type.contains("手机")) {
            return "电话";
        }
        if (type.contains("日期") || type.contains("时间")) {
            return "日期";
        }
        if (type.contains("数量") || type.contains("面积") || type.contains("规模")
            || type.contains("金额") || type.contains("工期") || type.contains("数字")) {
            return "数量";
        }
        return "其他";
    }

    // 候选词合并：同一词只保留一条，类型取优先级高的，来源优先标记为正则
    private void merge(Map<String, Candidate> candidates, Candidate candidate) {
        if (candidate == null || StringUtils.isBlank(candidate.text) || candidate.text.length() < 2) {
            return;
        }
        Candidate exists = candidates.get(candidate.text);
        if (exists == null) {
            candidates.put(candidate.text, candidate);
            return;
        }
        if (priority(candidate.type) > priority(exists.type)) {
            exists.type = candidate.type;
        }
        if (SOURCE_REGEX.equals(candidate.source)) {
            exists.source = SOURCE_REGEX;
        }
    }

    private int priority(String type) {
        Integer value = TYPE_PRIORITY.get(type);
        return value == null ? 0 : value;
    }

    private static class Candidate {

        private final String text;

        private String type;

        private String source;

        private Candidate(String text, String type, String source) {
            this.text = text;
            this.type = type;
            this.source = source;
        }
    }

    private static class AiResult {

        private int totalBatch;

        private int successBatch;

        private int candidateCount;
    }

    private static class RegexRule {

        private final String type;

        private final Pattern pattern;

        private RegexRule(String type, Pattern pattern) {
            this.type = type;
            this.pattern = pattern;
        }
    }

}
