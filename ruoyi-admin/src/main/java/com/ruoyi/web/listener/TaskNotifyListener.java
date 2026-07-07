package com.ruoyi.web.listener;

import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component("taskNotifyListener")
public class TaskNotifyListener implements TaskListener {

    private final ISysNotificationService notificationService;
    private final TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        if (!EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            return;
        }
        try {
            String taskId = delegateTask.getId();
            String taskName = delegateTask.getName();
            String assignee = delegateTask.getAssignee();
            String procInsId = delegateTask.getProcessInstanceId();
            Object businessKey = delegateTask.getVariable("businessKey");
            String bizKey = businessKey != null ? businessKey.toString() : null;

            if (StringUtils.isNotBlank(assignee)) {
                notificationService.createNotification(
                    Long.valueOf(assignee), "任务提醒",
                    "【" + taskName + "】需要您处理",
                    taskId, bizKey, procInsId
                );
            }

            List<org.flowable.identitylink.api.IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
            for (org.flowable.identitylink.api.IdentityLink link : links) {
                if ("candidate".equals(link.getType()) && StringUtils.isNotBlank(link.getUserId())) {
                    notificationService.createNotification(
                        Long.valueOf(link.getUserId()), "任务提醒",
                        "【" + taskName + "】需要您处理",
                        taskId, bizKey, procInsId
                    );
                }
            }
        } catch (Exception e) {
            log.warn("发送通知异常: {}", e.getMessage());
        }
    }
}