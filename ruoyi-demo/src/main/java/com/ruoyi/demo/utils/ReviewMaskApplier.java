package com.ruoyi.demo.utils;

import com.aspose.words.Body;
import com.aspose.words.Cell;
import com.aspose.words.CellCollection;
import com.aspose.words.Document;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.ParagraphCollection;
import com.aspose.words.Row;
import com.aspose.words.RowCollection;
import com.aspose.words.Run;
import com.aspose.words.RunCollection;
import com.aspose.words.SaveFormat;
import com.aspose.words.Table;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.common.exception.ServiceException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按已确认的敏感片段替换 docx 文本，保留 Run 格式。
 */
public final class ReviewMaskApplier {

    private ReviewMaskApplier() {
    }

    public static String apply(ReviewDoc doc, List<ReviewSpan> spans) {
        try {
            Document document = new Document(doc.getFilePath());
            Map<Long, Paragraph> paragraphMap = collectParagraphs(document);
            Map<Long, List<ReviewSpan>> groupByBlock = spans.stream()
                .collect(Collectors.groupingBy(ReviewSpan::getBlockId));

            for (Map.Entry<Long, List<ReviewSpan>> entry : groupByBlock.entrySet()) {
                Paragraph paragraph = paragraphMap.get(entry.getKey());
                if (paragraph == null) {
                    continue;
                }
                List<ReviewSpan> blockSpans = new ArrayList<>(entry.getValue());
                blockSpans.sort(Comparator.comparingInt(ReviewSpan::getStartChar).reversed());
                for (ReviewSpan span : blockSpans) {
                    applySpan(paragraph, span);
                }
            }

            String outputPath = doc.getFilePath().replaceFirst("\\.docx$", "_masked.docx");
            document.save(outputPath, SaveFormat.DOCX);
            return outputPath;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("脱敏处理失败: " + e.getMessage());
        }
    }

    private static Map<Long, Paragraph> collectParagraphs(Document document) {
        Map<Long, Paragraph> map = new HashMap<>();
        long blockId = 0;
        Body body = document.getFirstSection().getBody();
        NodeCollection children = body.getChildNodes(NodeType.ANY, false);
        for (int i = 0; i < children.getCount(); i++) {
            Node child = children.get(i);
            if (child.getNodeType() == NodeType.PARAGRAPH) {
                map.put(blockId++, (Paragraph) child);
            } else if (child.getNodeType() == NodeType.TABLE) {
                Table table = (Table) child;
                RowCollection rows = table.getRows();
                for (int r = 0; r < rows.getCount(); r++) {
                    Row row = rows.get(r);
                    CellCollection cells = row.getCells();
                    for (int c = 0; c < cells.getCount(); c++) {
                        Cell cell = cells.get(c);
                        ParagraphCollection paragraphs = cell.getParagraphs();
                        for (int pi = 0; pi < paragraphs.getCount(); pi++) {
                            map.put(blockId++, paragraphs.get(pi));
                        }
                    }
                }
            }
        }
        return map;
    }

    private static void applySpan(Paragraph paragraph, ReviewSpan span) {
        RunCollection runs = paragraph.getRuns();
        if (runs.getCount() == 0) {
            return;
        }
        List<String> texts = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < runs.getCount(); i++) {
            String text = ((Run) runs.get(i)).getText();
            texts.add(text);
            starts.add(total);
            total += text.length();
        }

        int start = Math.max(0, Math.min(total, span.getStartChar()));
        int end = Math.max(start, Math.min(total, span.getEndChar()));
        if (start >= end) {
            return;
        }

        String replacement = mask(span.getSpanType());
        boolean placed = false;
        for (int i = 0; i < texts.size(); i++) {
            int runStart = starts.get(i);
            int runEnd = runStart + texts.get(i).length();
            int overlapStart = Math.max(start, runStart);
            int overlapEnd = Math.min(end, runEnd);
            if (overlapStart >= overlapEnd) {
                continue;
            }
            String current = texts.get(i);
            String left = current.substring(0, overlapStart - runStart);
            String right = current.substring(overlapEnd - runStart);
            String newText = placed ? left + right : left + replacement + right;
            placed = true;
            ((Run) runs.get(i)).setText(newText);
        }
    }

    private static String mask(String spanType) {
        if (spanType == null) {
            return "【敏感】";
        }
        if (spanType.contains("人名")) {
            return "【人名】";
        }
        if (spanType.contains("地名")) {
            return "【地名】";
        }
        if (spanType.contains("机构") || spanType.contains("单位")) {
            return "【机构】";
        }
        if (spanType.contains("电话")) {
            return "【电话】";
        }
        return "【敏感】";
    }

}
