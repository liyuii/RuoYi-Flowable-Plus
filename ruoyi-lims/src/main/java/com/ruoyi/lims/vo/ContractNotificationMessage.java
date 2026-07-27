package com.ruoyi.lims.vo;

import lombok.Data;
import java.util.List;

@Data
public class ContractNotificationMessage {
    private Long contractId;
    private List<Long> approverIds;
    private String procInsId;
    private String taskId;
    private String eventType;
}
