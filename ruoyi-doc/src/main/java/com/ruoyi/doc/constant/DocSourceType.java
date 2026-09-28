package com.ruoyi.doc.constant;

/**
 * 文档库的来源类型：平台脱敏文件和用户上传模板是两条独立链路，只在文档库汇合。
 */
public final class DocSourceType {

    /**
     * 平台原始投标文件（拆分/识别/脱敏/转图片后同步过来的脱敏文件）
     */
    public static final String REVIEW_MASK = "REVIEW_MASK";

    /**
     * 用户上传的可直接使用模板（审核通过后直接发布，未做任何处理）
     */
    public static final String USER_TEMPLATE = "USER_TEMPLATE";

    private DocSourceType() {
    }

}
