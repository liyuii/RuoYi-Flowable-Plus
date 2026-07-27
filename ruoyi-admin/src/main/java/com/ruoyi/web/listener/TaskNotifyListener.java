package com.ruoyi.web.listener;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.lims.vo.ContractNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component("taskNotifyListener")
public class TaskNotifyListener implements TaskListener {

    private final RabbitTemplate rabbitTemplate;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        if (!EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            return;
        }
        try {
            // 从流程实例的 businessKey 中解析合同 ID
            String procInsId = delegateTask.getProcessInstanceId();
            ProcessInstance procInst = runtimeService.createProcessInstanceQuery()
                .processInstanceId(procInsId)
                .singleResult();
            if (procInst == null || procInst.getBusinessKey() == null) {
                log.warn("未找到流程实例或 businessKey: procInsId={}", procInsId);
                return;
            }
            String businessKey = procInst.getBusinessKey();
            // businessKey 格式为 "contract_{id}"
            if (!businessKey.startsWith("contract_")) {
                return;  // 不是合同流程，不处理
            }
            Long contractId = Long.parseLong(businessKey.replace("contract_", ""));

            // 收集审批人 ID
            List<Long> approverIds = new ArrayList<>();
            String assignee = delegateTask.getAssignee();
            if (StringUtils.isNotBlank(assignee)) {
                approverIds.add(Long.parseLong(assignee));
            }

            // 如果没有 assignee，取候选人
            if (approverIds.isEmpty()) {
                taskService.getIdentityLinksForTask(delegateTask.getId()).stream()
                    .filter(link -> "candidate".equals(link.getType())
                        && StringUtils.isNotBlank(link.getUserId()))
                    .forEach(link -> approverIds.add(Long.parseLong(link.getUserId())));
            }

            if (approverIds.isEmpty()) {
                log.warn("未找到审批人: taskId={}", delegateTask.getId());
                return;
            }

            // 发送消息到 RabbitMQ
            ContractNotificationMessage msg = new ContractNotificationMessage();
            msg.setContractId(contractId);
            msg.setApproverIds(approverIds);
            msg.setProcInsId(procInsId);
            msg.setTaskId(delegateTask.getId());
            msg.setEventType("task_created");

            rabbitTemplate.convertAndSend(
                "contract.notification.exchange",
                "contract.notification.approve",
                msg);

            log.info("已发送合同审批通知消息: contractId={}, approvers={}", contractId, approverIds);

        } catch (Exception e) {
            log.error("合同审批通知发送异常: {}", e.getMessage(), e);
        }
    }
}
