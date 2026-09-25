package com.ruoyi.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 投标方案库定时任务开关。
 * <p>
 * 当前用 Spring 的 @Scheduled 跑（转图片兜底 + 文档同步），
 * 将来要换成 XXL-Job 只需要在 Job 方法上加 @XxlJob 注解，业务代码不用动。
 */
@Configuration
@EnableScheduling
public class ReviewScheduleConfig {

}
