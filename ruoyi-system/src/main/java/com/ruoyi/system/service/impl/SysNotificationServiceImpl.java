package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.system.domain.SysNotification;
import com.ruoyi.system.mapper.SysNotificationMapper;
import com.ruoyi.system.service.ISysNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SysNotificationServiceImpl implements ISysNotificationService {

    private final SysNotificationMapper baseMapper;

    @Override
    public void createNotification(Long userId, String title, String content, String taskId, String businessKey, String procInsId) {
        SysNotification notif = new SysNotification();
        notif.setUserId(userId);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setTaskId(taskId);
        notif.setBusinessKey(businessKey);
        notif.setProcInsId(procInsId);
        notif.setIsRead("0");
        notif.setCreateTime(new Date());
        baseMapper.insert(notif);
    }

    @Override
    public List<SysNotification> listByUser(Long userId, int limit) {
        LambdaQueryWrapper<SysNotification> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotification::getUserId, userId);
        lqw.orderByDesc(SysNotification::getId);
        lqw.last("LIMIT " + limit);
        return baseMapper.selectList(lqw);
    }

    @Override
    public long unreadCount(Long userId) {
        LambdaQueryWrapper<SysNotification> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotification::getUserId, userId);
        lqw.eq(SysNotification::getIsRead, "0");
        return baseMapper.selectCount(lqw);
    }

    @Override
    public boolean markAsRead(Long id) {
        SysNotification update = new SysNotification();
        update.setId(id);
        update.setIsRead("1");
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        SysNotification update = new SysNotification();
        update.setIsRead("1");
        LambdaQueryWrapper<SysNotification> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysNotification::getUserId, userId);
        lqw.eq(SysNotification::getIsRead, "0");
        return baseMapper.update(update, lqw) > 0;
    }
}