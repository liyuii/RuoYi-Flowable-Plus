package com.ruoyi.lims.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽取结果返回对象。
 * success 为 false 时表示这次没抽成，checks 里是每条范围差几个人，前端按这个提示用户。
 * success 为 true 时 results 里是抽到的人。
 */
@Data
public class ExpertDrawResultVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否抽满 */
    private Boolean success = Boolean.TRUE;

    /** 提示信息 */
    private String message;

    /** 每条范围的预检情况 */
    private List<ExpertDrawCheckVo> checks = new ArrayList<>();

    /** 抽到的结果 */
    private List<ExpertExtractResultVo> results = new ArrayList<>();
}
