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
import com.aspose.words.Table;
import com.ruoyi.demo.domain.vo.ReviewCellVO;
import com.ruoyi.demo.domain.vo.ReviewNodeVO;
import com.ruoyi.demo.domain.vo.ReviewParagraphVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 Aspose.Words 解析 docx，生成结构化的审核展示节点。
 */
public final class ReviewDocxParser {

    private ReviewDocxParser() {
    }

    public static List<ReviewNodeVO> parse(String filePath) throws Exception {
        Document document = new Document(filePath);
        List<ReviewNodeVO> nodes = new ArrayList<>();
        long blockId = 0;
        Body body = document.getFirstSection().getBody();
        NodeCollection children = body.getChildNodes(NodeType.ANY, false);
        for (int i = 0; i < children.getCount(); i++) {
            Node child = children.get(i);
            if (child.getNodeType() == NodeType.PARAGRAPH) {
                ReviewNodeVO nodeVO = new ReviewNodeVO();
                nodeVO.setType("PARAGRAPH");
                nodeVO.setBlockId(blockId++);
                nodeVO.setText(paragraphText((Paragraph) child));
                nodes.add(nodeVO);
            } else if (child.getNodeType() == NodeType.TABLE) {
                ReviewNodeVO nodeVO = new ReviewNodeVO();
                nodeVO.setType("TABLE");
                nodeVO.setRows(parseTable((Table) child, blockId));
                blockId = lastBlockId(nodeVO) + 1;
                nodes.add(nodeVO);
            }
        }
        return nodes;
    }

    private static List<List<ReviewCellVO>> parseTable(Table table, long blockId) {
        List<List<ReviewCellVO>> rows = new ArrayList<>();
        RowCollection rowCollection = table.getRows();
        for (int r = 0; r < rowCollection.getCount(); r++) {
            Row row = rowCollection.get(r);
            List<ReviewCellVO> rowCells = new ArrayList<>();
            CellCollection cells = row.getCells();
            for (int c = 0; c < cells.getCount(); c++) {
                Cell cell = cells.get(c);
                ReviewCellVO cellVO = new ReviewCellVO();
                List<ReviewParagraphVO> paragraphs = new ArrayList<>();
                ParagraphCollection paragraphCollection = cell.getParagraphs();
                for (int pi = 0; pi < paragraphCollection.getCount(); pi++) {
                    Paragraph paragraph = paragraphCollection.get(pi);
                    ReviewParagraphVO paragraphVO = new ReviewParagraphVO();
                    paragraphVO.setBlockId(blockId++);
                    paragraphVO.setText(paragraphText(paragraph));
                    paragraphs.add(paragraphVO);
                }
                cellVO.setParagraphs(paragraphs);
                rowCells.add(cellVO);
            }
            rows.add(rowCells);
        }
        return rows;
    }

    private static long lastBlockId(ReviewNodeVO tableVO) {
        long last = 0;
        if (tableVO.getRows() == null) {
            return 0;
        }
        for (List<ReviewCellVO> row : tableVO.getRows()) {
            for (ReviewCellVO cell : row) {
                if (cell.getParagraphs() != null) {
                    for (ReviewParagraphVO paragraphVO : cell.getParagraphs()) {
                        last = Math.max(last, paragraphVO.getBlockId());
                    }
                }
            }
        }
        return last;
    }

    static String paragraphText(Paragraph paragraph) {
        StringBuilder builder = new StringBuilder();
        RunCollection runs = paragraph.getRuns();
        for (int i = 0; i < runs.getCount(); i++) {
            Run run = (Run) runs.get(i);
            builder.append(run.getText());
        }
        return builder.toString();
    }

}
