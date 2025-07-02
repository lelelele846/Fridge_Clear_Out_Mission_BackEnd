package com.example.backend.service;

import com.example.backend.exception.ExpirationWarningException;
import com.example.backend.pojo.Food;

import java.util.List;

public interface ExpirationWarningService {
    /**
     * 检查食物过期状态并更新对象
     * @param food 待检查的食物对象
     * @throws ExpirationWarningException 当检查失败时抛出
     */
    void checkAndWarnExpiration(Food food) throws ExpirationWarningException;

    /**
     * 计算剩余保质期天数
     * @param food 食物对象
     * @return 剩余天数（负数表示已过期）
     * @throws ExpirationWarningException 当计算失败时抛出
     */
    long calculateRemainingDays(Food food) throws ExpirationWarningException;

    /**
     * 判断是否即将过期（3天内）
     */
    boolean isExpiringSoon(Food food) throws ExpirationWarningException;

    /**
     * 判断是否已过期
     */
    boolean isExpired(Food food) throws ExpirationWarningException;

    /**
     * 批量检查食物过期状态
     * @param foods 食物列表
     * @return 更新后的食物列表
     */
    List<Food> batchCheckExpiration(List<Food> foods);

    /**
     * 获取即将过期的食物列表
     * @param fridgeId 冰箱ID
     * @return 即将过期的食物列表
     */
    List<Food> getExpiringSoonFoods(Integer fridgeId);

    /**
     * 获取已过期的食物列表
     * @param fridgeId 冰箱ID
     * @return 已过期的食物列表
     */
    List<Food> getExpiredFoods(Integer fridgeId);

    /**
     * 定时更新所有食物状态
     */
    void updateAllFoodExpirationStatus();

    /**
     * 验证食物是否可以添加到冰箱
     * @param food 待添加的食物
     * @throws ExpirationWarningException 如果食物已过期
     */
    void validateFoodForAddition(Food food) throws ExpirationWarningException;
}
