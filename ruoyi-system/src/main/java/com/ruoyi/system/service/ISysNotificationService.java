package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysNotification;
import java.util.List;

public interface ISysNotificationService {
    void createNotification(Long userId, String title, String content, String taskId, String businessKey, String procInsId);
    List<SysNotification> listByUser(Long userId, int limit);
    long unreadCount(Long userId);
    boolean markAsRead(Long id);
    boolean markAllAsRead(Long userId);
}