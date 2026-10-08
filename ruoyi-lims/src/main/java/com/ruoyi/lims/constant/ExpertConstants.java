package com.ruoyi.lims.constant;

/**
 * 专家抽取相关常量
 */
public interface ExpertConstants {

    /** 抽取申请状态：草稿 */
    String EXTRACT_DRAFT = "DRAFT";

    /** 抽取申请状态：待审核 */
    String EXTRACT_WAIT_AUDIT = "WAIT_AUDIT";

    /** 抽取申请状态：待抽取（审核通过） */
    String EXTRACT_WAIT_EXTRACT = "WAIT_EXTRACT";

    /** 抽取申请状态：已抽取 */
    String EXTRACT_EXTRACTED = "EXTRACTED";

    /** 抽取申请状态：补充抽取 */
    String EXTRACT_WAIT_SUPPLEMENT = "WAIT_SUPPLEMENT";

    /** 抽取申请状态：审核驳回 */
    String EXTRACT_REJECTED = "REJECTED";

    /** 专家状态：待确认（占用） */
    String RESULT_WAIT_CONFIRM = "WAIT_CONFIRM";

    /** 专家状态：参加（占用） */
    String RESULT_JOIN = "JOIN";

    /** 专家状态：不参加（不占用） */
    String RESULT_QUIT = "QUIT";

    /** 专家状态：请假（不占用） */
    String RESULT_LEAVE = "LEAVE";

    /** 抽取类型：首次抽取 */
    String DRAW_TYPE_FIRST = "FIRST";

    /** 抽取类型：补充抽取 */
    String DRAW_TYPE_SUPPLEMENT = "SUPPLEMENT";

    /** 回避类型：专家 */
    String AVOID_TYPE_EXPERT = "EXPERT";

    /** 回避类型：单位 */
    String AVOID_TYPE_COMPANY = "COMPANY";

    /** 专家状态：正常 */
    String EXPERT_STATUS_NORMAL = "0";

    /** 专家状态：停用 */
    String EXPERT_STATUS_DISABLED = "1";
}
