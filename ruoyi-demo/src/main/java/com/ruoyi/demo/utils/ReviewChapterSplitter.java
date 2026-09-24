package com.ruoyi.demo.utils;

import com.aspose.words.Body;
import com.aspose.words.Document;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.OutlineLevel;
import com.aspose.words.Paragraph;
import com.aspose.words.SaveFormat;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 投标文件章节拆分：把标题命中关键字的章节整段提取成一个新的 docx。
 * <p>
 * 标题识别顺序：大纲级别 > 标题样式 > 标题文本正则（仅在文档完全没有样式信息时兜底）。
 * 章节边界：以“第X章”所在层级作为章节层级，每章从标题节点开始，到下一个同级或更高级标题之前结束。
 * 生成方式：重新打开原文后删除未命中的节点，保留原有样式、表格结构与节属性。
 */
public final class ReviewChapterSplitter {

    /**
     * 章节标题：第X章 / 第X篇
     */
    private static final Pattern CHAPTER_TITLE = Pattern.compile("^\\s*第[一二三四五六七八九十百千零〇0-9]+\\s*[章篇]");

    /**
     * 标题样式：Heading 1 / 标题 1
     */
    private static final Pattern HEADING_STYLE = Pattern.compile("(?i)^\\s*(heading|标题)\\s*([1-9])\\s*$");

    /**
     * 文本兜底：第X章
     */
    private static final Pattern TEXT_CHAPTER = Pattern.compile("^\\s*第[一二三四五六七八九十百千零〇0-9]+\\s*章.*");

    /**
     * 文本兜底：第X节
     */
    private static final Pattern TEXT_SECTION = Pattern.compile("^\\s*第[一二三四五六七八九十百千零〇0-9]+\\s*节.*");

    /**
     * 文本兜底：1 / 1.1 / 1.1.1 这类编号标题
     */
    private static final Pattern TEXT_NUMBERED = Pattern.compile("^\\s*(\\d+(\\.\\d+)*)\\s+\\S.*");

    /**
     * 标题层级最大值，与 Word 的 1~9 级标题对应
     */
    private static final int MAX_HEADING_LEVEL = 9;

    private ReviewChapterSplitter() {
    }

    /**
     * 提取命中关键字的章节，生成 outputPath，返回命中的章节标题。
     */
    public static List<String> split(String sourcePath, String outputPath, List<String> keywords) throws Exception {
        Document document = new Document(sourcePath);
        Body body = document.getFirstSection().getBody();
        NodeCollection children = body.getChildNodes(NodeType.ANY, false);
        List<Node> nodes = new ArrayList<>(children.getCount());
        for (int i = 0; i < children.getCount(); i++) {
            nodes.add(children.get(i));
        }

        List<HeadingInfo> headings = collectHeadings(nodes);
        if (headings.isEmpty()) {
            throw new ServiceException("未识别到章节标题，无法拆分章节");
        }
        int chapterLevel = resolveChapterLevel(headings);
        List<Chapter> chapters = buildChapters(headings, nodes.size(), chapterLevel);
        List<Chapter> matched = new ArrayList<>();
        for (Chapter chapter : chapters) {
            if (matchesKeywords(chapter.title, keywords)) {
                matched.add(chapter);
            }
        }
        if (matched.isEmpty()) {
            throw new ServiceException("未找到标题包含【" + String.join("、", keywords) + "】的章节，已识别章节："
                + chapterTitles(chapters) + "；可在 ruoyi.review.split-keywords 中调整关键词");
        }

        boolean[] keep = new boolean[nodes.size()];
        for (Chapter chapter : matched) {
            for (int i = chapter.start; i < chapter.end; i++) {
                keep[i] = true;
            }
        }
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (!keep[i]) {
                nodes.get(i).remove();
            }
        }

        if (StringUtils.isBlank(document.getText())) {
            throw new ServiceException("拆分结果为空，请检查文档章节结构");
        }
        document.save(outputPath, SaveFormat.DOCX);

        List<String> result = new ArrayList<>(matched.size());
        for (Chapter chapter : matched) {
            result.add(chapter.title);
        }
        return result;
    }

    /**
     * 收集标题节点。优先使用样式信息，完全没有样式信息时才按标题文本判断，避免把正文里的编号误判成标题。
     */
    private static List<HeadingInfo> collectHeadings(List<Node> nodes) {
        List<HeadingInfo> headings = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            String text = paragraphText(node);
            if (text.isEmpty()) {
                continue;
            }
            int level = styleOrOutlineLevel((Paragraph) node);
            if (level > 0) {
                headings.add(new HeadingInfo(i, level, text));
            }
        }
        if (!headings.isEmpty()) {
            return headings;
        }
        for (int i = 0; i < nodes.size(); i++) {
            String text = paragraphText(nodes.get(i));
            if (text.isEmpty()) {
                continue;
            }
            int level = textLevel(text);
            if (level > 0) {
                headings.add(new HeadingInfo(i, level, text));
            }
        }
        return headings;
    }

    private static String paragraphText(Node node) {
        if (node.getNodeType() != NodeType.PARAGRAPH) {
            return "";
        }
        return ReviewDocxParser.paragraphText((Paragraph) node).trim();
    }

    private static int styleOrOutlineLevel(Paragraph paragraph) {
        int outlineLevel = paragraph.getParagraphFormat().getOutlineLevel();
        if (outlineLevel >= OutlineLevel.LEVEL_1 && outlineLevel <= OutlineLevel.LEVEL_9) {
            // Aspose 的 OutlineLevel 从 0 开始（LEVEL_1=0、BODY_TEXT=9），统一转成 1~9
            return outlineLevel - OutlineLevel.LEVEL_1 + 1;
        }
        String styleName = paragraph.getParagraphFormat().getStyleName();
        if (StringUtils.isNotBlank(styleName)) {
            Matcher matcher = HEADING_STYLE.matcher(styleName.trim());
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(2));
            }
        }
        return 0;
    }

    private static int textLevel(String text) {
        if (TEXT_CHAPTER.matcher(text).matches()) {
            return 1;
        }
        if (TEXT_SECTION.matcher(text).matches()) {
            return 2;
        }
        Matcher matcher = TEXT_NUMBERED.matcher(text);
        if (matcher.matches()) {
            int dots = matcher.group(1).split("\\.").length - 1;
            return Math.min(1 + dots, MAX_HEADING_LEVEL);
        }
        return 0;
    }

    /**
     * 章节层级：优先取“第X章”标题所在的层级，没有这种标题时取最外层标题层级。
     */
    private static int resolveChapterLevel(List<HeadingInfo> headings) {
        int chapterLevel = Integer.MAX_VALUE;
        for (HeadingInfo heading : headings) {
            if (CHAPTER_TITLE.matcher(heading.title).find()) {
                chapterLevel = Math.min(chapterLevel, heading.level);
            }
        }
        if (chapterLevel != Integer.MAX_VALUE) {
            return chapterLevel;
        }
        for (HeadingInfo heading : headings) {
            chapterLevel = Math.min(chapterLevel, heading.level);
        }
        return chapterLevel;
    }

    private static List<Chapter> buildChapters(List<HeadingInfo> headings, int totalNodes, int chapterLevel) {
        List<Chapter> chapters = new ArrayList<>();
        Chapter current = null;
        for (HeadingInfo heading : headings) {
            if (heading.level <= chapterLevel) {
                if (current != null) {
                    current.end = heading.index;
                    chapters.add(current);
                }
                current = new Chapter(heading.index, heading.title);
            }
        }
        if (current != null) {
            current.end = totalNodes;
            chapters.add(current);
        }
        return chapters;
    }

    private static boolean matchesKeywords(String title, List<String> keywords) {
        for (String keyword : keywords) {
            if (StringUtils.isNotBlank(keyword) && title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String chapterTitles(List<Chapter> chapters) {
        StringBuilder builder = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (builder.length() > 0) {
                builder.append("；");
            }
            builder.append(chapter.title);
        }
        return StringUtils.substring(builder.toString(), 0, 400);
    }

    private static final class HeadingInfo {

        private final int index;

        private final int level;

        private final String title;

        private HeadingInfo(int index, int level, String title) {
            this.index = index;
            this.level = level;
            this.title = title;
        }
    }

    private static final class Chapter {

        private final int start;

        private final String title;

        private int end;

        private Chapter(int start, String title) {
            this.start = start;
            this.title = title;
        }
    }
}
