package com.ruoyi.lims.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 报检单主表对象 lims_inspection
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lims_inspection")
public class LimsInspection extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inspectionNo;

    private Long specId;

    private String batchNo;

    private Date sampleDate;

    private String samplePlace;

    private String applicant;

    private String status;

    private String resultVerdict;

    private String remark;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;
}
