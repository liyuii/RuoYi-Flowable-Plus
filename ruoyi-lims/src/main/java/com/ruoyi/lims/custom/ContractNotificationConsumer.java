package com.ruoyi.lims.custom;

import com.rabbitmq.client.Channel;
import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.service.IContractApproveService;
import com.ruoyi.lims.vo.ContractNotificationMessage;
import com.ruoyi.system.service.ISysNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ContractNotificationConsumer {

    @Autowired
    private ISysNotificationService notificationService;

    @Autowired
    private IContractApproveService contractService;

    @RabbitListener(queues = "contract.notification.queue")
    public void handleNotification(ContractNotificationMessage msg,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("收到合同审批通知消息: contractId={}, approvers={}",
                msg.getContractId(), msg.getApproverIds());

            // 查询合同名称
            String contractName = getContractName(msg.getContractId());

            for (Long approverId : msg.getApproverIds()) {
                notificationService.createNotification(
                    approverId,
                    "合同审批通知",
                    "有一份合同「" + contractName + "」待您审批",
                    msg.getTaskId(),
                    String.valueOf(msg.getContractId()),
                    msg.getProcInsId()
                );
            }

            channel.basicAck(tag, false);
            log.info("合同审批通知发送完成: contractId={}", msg.getContractId());

        } catch (Exception e) {
            log.error("合同审批通知处理失败: contractId={}", msg.getContractId(), e);
            channel.basicNack(tag, false, true);
        }
    }

    private String getContractName(Long contractId) {
        if (contractId == null) return "未知合同";
        ContractApprove contract = contractService.queryById(contractId);
        return contract != null ? contract.getContractName() : "未知合同";
    }
}
