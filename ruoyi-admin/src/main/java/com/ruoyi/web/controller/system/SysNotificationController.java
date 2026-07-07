package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.system.domain.SysNotification;
import com.ruoyi.system.service.ISysNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notification")
public class SysNotificationController extends BaseController {

    private final ISysNotificationService notificationService;

    @GetMapping("/list")
    public R<List<SysNotification>> list() {
        Long userId = LoginHelper.getUserId();
        return R.ok(notificationService.listByUser(userId, 20));
    }

    @GetMapping("/unreadCount")
    public R<Long> unreadCount() {
        Long userId = LoginHelper.getUserId();
        return R.ok(notificationService.unreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        return toAjax(notificationService.markAsRead(id));
    }

    @PutMapping("/readAll")
    public R<Void> markAllAsRead() {
        Long userId = LoginHelper.getUserId();
        return toAjax(notificationService.markAllAsRead(userId));
    }
}