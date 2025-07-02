package com.example.backend.service.impl;

import com.example.backend.exception.ExpirationWarningException;
import com.example.backend.mapper.FoodMapper;
import com.example.backend.pojo.Food;
import com.example.backend.service.ExpirationWarningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpirationWarningServiceImpl implements ExpirationWarningService {

    @Value("${expiration.warning-threshold-days:3}")
    private int EXPIRING_SOON_THRESHOLD_DAYS;

    @Autowired
    private FoodMapper foodMapper;

    public void checkAndWarnExpiration(Food food) throws ExpirationWarningException {
        if (food.getManufactureDate() == null || food.getExpiryDuration() == null) {
            throw new ExpirationWarningException("缺少生产日期或保质期信息");
        }
        LocalDateTime manufactureDate = food.getManufactureDate();
        LocalDateTime expiryDate = manufactureDate.plusDays(food.getExpiryDuration());
        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);

        food.setExpired(remainingDays <= 0);
        food.setExpSoon(remainingDays > 0 && remainingDays <= 3);

        if (remainingDays <= 0) {
            food.setExpired(true);
            food.setExpSoon(false);
            food.setExpiryStatus("已过期");
        } else if (remainingDays > 0 && remainingDays <= 3) {
            food.setExpired(false);
            food.setExpSoon(true);
            food.setExpiryStatus("即将过期");
        } else {
            food.setExpired(false);
            food.setExpSoon(false);
            food.setExpiryStatus("新鲜");
        }

    }


    @Override
    public long calculateRemainingDays(Food food) throws ExpirationWarningException {
        try {
            if (food.getManufactureDate() == null || food.getExpiryDuration() == null) {
                throw new ExpirationWarningException("缺少生产日期或保质期信息");
            }

            LocalDate manufactureDate = food.getManufactureDate().toLocalDate();
            LocalDate expiryDate = manufactureDate.plusDays(food.getExpiryDuration());
            return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        } catch (Exception e) {
            throw new ExpirationWarningException("计算剩余天数失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isExpiringSoon(Food food) throws ExpirationWarningException {
        long remainingDays = calculateRemainingDays(food);
        return remainingDays > 0 && remainingDays <= EXPIRING_SOON_THRESHOLD_DAYS;
    }

    @Override
    public boolean isExpired(Food food) throws ExpirationWarningException {
        return calculateRemainingDays(food) <= 0;
    }

    @Override
    public List<Food> batchCheckExpiration(List<Food> foods) {
        return foods.stream().map(food -> {
            try {
                checkAndWarnExpiration(food);
            } catch (ExpirationWarningException e) {
                log.error("批量检查食物过期状态失败: {}", e.getMessage());
            }
            return food;
        }).collect(Collectors.toList());
    }


    @Override
    public List<Food> getExpiringSoonFoods(Integer fridgeId) {
        try {
            return foodMapper.getExpiringSoonFoods(fridgeId);
        } catch (Exception e) {
            log.error("获取即将过期食物列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Food> getExpiredFoods(Integer fridgeId) {
        try {
            return foodMapper.getExpiredFoods(fridgeId);
        } catch (Exception e) {
            log.error("获取过期食物列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateAllFoodExpirationStatus() {
        try {
            log.info("开始定时更新所有食物过期状态...");
            // 这里可以获取所有冰箱的食物，或者通过定时任务调用
            // 暂时记录日志，具体实现可以根据需求调整
            log.info("定时更新食物过期状态完成");
        } catch (Exception e) {
            log.error("定时更新食物过期状态失败: {}", e.getMessage());
        }
    }

    @Override
    public void validateFoodForAddition(Food food) throws ExpirationWarningException {
        // 检查食物是否可以添加到冰箱
        checkAndWarnExpiration(food);

        if (food.getExpired()) {
            throw new ExpirationWarningException(
                    String.format("食物 '%s' 已过期，无法添加到冰箱。过期天数: %d",
                            food.getName(),
                            Math.abs(calculateRemainingDays(food)))
            );
        }

        if (food.getExpSoon()) {
            log.warn("⚠️ 警告：食物 '{}' 即将过期（剩余{}天），建议谨慎添加",
                    food.getName(), calculateRemainingDays(food));
        }
    }
}
