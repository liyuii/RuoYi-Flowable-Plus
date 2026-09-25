package com.ruoyi.demo.utils;

import com.aspose.words.Document;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.Run;
import com.aspose.words.RunCollection;
import com.aspose.words.SaveFormat;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.enums.ReviewMaskEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 把已确认的敏感词替换成类型化掩码（如【机构名】），结果另存为新文件，不覆盖源文件。
 * <p>
 * 识别阶段只记录“词”，不记录位置，所以这里按段落文本匹配全部出现位置：
 * 1. 同一个位置能命中多个词时按长度优先（“石家庄市”优先于“石家庄”）；
 * 2. 敏感词跨多个 Run（Word 内部按格式切分的片段）时，掩码写在起始 Run 上，保留该处格式；
 * 3. 单字词在全文替换时误伤面太大，直接跳过并留日志。
 */
@Slf4j
public final class ReviewMaskApplier {

    /**
     * 参与替换的最小词长
     */
    private static final int MIN_WORD_LENGTH = 2;

    private ReviewMaskApplier() {
    }

    /**
     * @param sourcePath 源文件（截取文件）路径
     * @param spans      已确认的敏感词
     * @param outputPath 脱敏文件输出路径
     * @return 实际替换的处数
     */
    public static int apply(String sourcePath, List<ReviewSpan> spans, String outputPath) {
        List<Word> words = buildWords(spans);
        try {
            Document document = new Document(sourcePath);
            // deep = true，正文段落和表格单元格内段落一起处理
            NodeCollection paragraphs = document.getChildNodes(NodeType.PARAGRAPH, true);
            int replaced = 0;
            for (int i = 0; i < paragraphs.getCount(); i++) {
                replaced += replaceInParagraph((Paragraph) paragraphs.get(i), words);
            }
            document.save(outputPath, SaveFormat.DOCX);
            return replaced;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("脱敏处理失败: " + e.getMessage());
        }
    }

    /**
     * 把已确认的敏感词整理成替换词典：去重、过滤过短词、按词长倒序（长词优先匹配）。
     */
    private static List<Word> buildWords(List<ReviewSpan> spans) {
        List<Word> words = new ArrayList<>();
        Set<String> exists = new HashSet<>();
        for (ReviewSpan span : spans) {
            String text = span.getSpanText() == null ? "" : span.getSpanText().trim();
            if (text.isEmpty() || !exists.add(text)) {
                continue;
            }
            if (text.length() < MIN_WORD_LENGTH) {
                log.warn("敏感词长度小于{}，已跳过替换 spanId={} word={}", MIN_WORD_LENGTH, span.getId(), text);
                continue;
            }
            words.add(new Word(text, ReviewMaskEnum.maskOf(span.getSpanType())));
        }
        words.sort(Comparator.comparingInt((Word word) -> word.text.length()).reversed());
        return words;
    }

    /**
     * 替换单个段落里的敏感词，返回替换处数。
     */
    private static int replaceInParagraph(Paragraph paragraph, List<Word> words) {
        if (words.isEmpty()) {
            return 0;
        }
        RunCollection runs = paragraph.getRuns();
        int count = runs.getCount();
        if (count == 0) {
            return 0;
        }
        String[] texts = new String[count];
        int[] starts = new int[count];
        int total = 0;
        for (int i = 0; i < count; i++) {
            texts[i] = ((Run) runs.get(i)).getText();
            starts[i] = total;
            total += texts[i].length();
        }
        if (total == 0) {
            return 0;
        }
        String full = String.join("", texts);

        int position = 0;
        int hits = 0;
        for (int i = 0; i < count; i++) {
            StringBuilder text = new StringBuilder(texts[i].length());
            int runEnd = starts[i] + texts[i].length();
            while (position < runEnd) {
                Word word = match(full, position, words);
                if (word != null) {
                    // 命中可能跨多个 Run，掩码只写在起始 Run 上，被覆盖的字符随 position 跳过
                    text.append(word.mask);
                    position += word.text.length();
                    hits++;
                } else {
                    text.append(full.charAt(position));
                    position++;
                }
            }
            ((Run) runs.get(i)).setText(text.toString());
        }
        return hits;
    }

    private static Word match(String text, int position, List<Word> words) {
        for (Word word : words) {
            if (text.startsWith(word.text, position)) {
                return word;
            }
        }
        return null;
    }

    private static class Word {

        private final String text;

        private final String mask;

        private Word(String text, String mask) {
            this.text = text;
            this.mask = mask;
        }
    }

}
