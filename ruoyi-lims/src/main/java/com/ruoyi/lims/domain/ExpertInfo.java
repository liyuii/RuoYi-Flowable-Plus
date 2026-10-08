package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 评标专家对象 expert_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert_info")
public class ExpertInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 专家姓名 */
    private String expertName;

    /** 身份证号 */
    private String idCard;

    /** 联系电话 */
    private String phone;

    /** 工作单位名称 */
    private String companyName;

    /** 所属地区编码 */
    private String regionCode;

    /** 状态（0正常 1停用） */
    private String status;

    /** 备注 */
    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    /** 专业编码集合，保存和编辑时使用，不入库 */
    @TableField(exist = false)
    private List<String> majorCodes;

    /** 专业编码，列表查询条件用，不入库 */
    @TableField(exist = false)
    private String majorCode;

    /** 专业名称，列表展示用，不入库 */
    @TableField(exist = false)
    private String majorNames;
}
