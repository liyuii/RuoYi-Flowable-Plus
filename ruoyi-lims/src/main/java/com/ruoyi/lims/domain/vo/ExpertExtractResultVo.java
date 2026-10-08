package com.ruoyi.lims.domain.vo;

import com.ruoyi.lims.domain.ExpertExtractResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专家抽取结果展示对象，在结果表基础上带出专家信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertExtractResultVo extends ExpertExtractResult {

    private static final long serialVersionUID = 1L;

    /** 专家姓名 */
    private String expertName;

    /** 联系电话 */
    private String phone;

    /** 工作单位名称 */
    private String companyName;

    /** 所属地区编码 */
    private String regionCode;

    /** 所属范围的专业编码 */
    private String majorCode;

    /** 所属范围的专业名称 */
    private String majorName;
}
