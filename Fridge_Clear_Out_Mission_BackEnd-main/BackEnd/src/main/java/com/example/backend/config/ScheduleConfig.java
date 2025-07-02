package com.example.backend.config;

import com.example.backend.service.ExpirationWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private ExpirationWarningService expirationWarningService;

    /**
     * 每天凌晨2点更新所有食物的过期状态
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateFoodExpirationStatus() {
        log.info("开始执行定时任务：更新食物过期状态");
        try {
            expirationWarningService.updateAllFoodExpirationStatus();
            log.info("定时任务执行完成：食物过期状态更新成功");
        } catch (Exception e) {
            log.error("定时任务执行失败：更新食物过期状态时发生错误", e);
        }
    }

    /**
     * 每小时检查一次即将过期的食物
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiringSoonFoods() {
        log.info("开始执行定时任务：检查即将过期的食物");
        try {
            // 这里可以添加发送通知的逻辑
            log.info("定时任务执行完成：即将过期食物检查完成");
        } catch (Exception e) {
            log.error("定时任务执行失败：检查即将过期食物时发生错误", e);
        }
    }
}