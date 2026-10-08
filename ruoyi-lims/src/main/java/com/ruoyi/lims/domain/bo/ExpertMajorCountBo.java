package com.ruoyi.lims.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 统计各专业可抽取人数的请求对象
 */
@Data
public class ExpertMajorCountBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 抽取地区编码 */
    private String regionCode;

    /** 评标时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date evaluateTime;

    /** 表单里当前填的回避专家，格式为 姓名|身份证号 */
    private List<String> expertKeys;

    /** 表单里当前填的回避单位名称 */
    private List<String> companies;
}
