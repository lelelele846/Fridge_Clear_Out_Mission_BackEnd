package com.example.backend.service.impl;

import com.example.backend.exception.ExpirationWarningException;
import com.example.backend.exception.FoodExpiredException;
import com.example.backend.mapper.FoodMapper;
import com.example.backend.mapper.FridgeMapper;
import com.example.backend.pojo.Food;
import com.example.backend.service.FoodService;
import com.example.backend.service.ExpirationWarningService;
import com.example.backend.utils.CurrentHold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private FridgeMapper fridgeMapper;

    @Autowired
    private ExpirationWarningService expiryCheckService;

    @Override
    @Transactional
    public void add(Food food) throws FoodExpiredException {
        try {
            // 1. 验证食物是否可以添加
            expiryCheckService.validateFoodForAddition(food);

            // 2. 设置基础时间信息
            LocalDateTime now = LocalDateTime.now();
            food.setCreateTime(now);
            food.setUpdateTime(now);

            // 3. 计算过期日期
            if (food.getManufactureDate() == null) {
                food.setManufactureDate(now); // 默认使用当前时间作为生产日期
            }
            if (food.getExpiryDuration() == null) {
                food.setExpiryDuration(7); // 默认7天保质期
            }
            food.setExpiryDate(food.getManufactureDate().plusDays(food.getExpiryDuration()).toLocalDate());

            // 4. 关联冰箱
            Integer fridgeId = fridgeMapper.selectByUserId(getCurrentUserId());
            if (fridgeId == null) {
                throw new IllegalStateException("用户未绑定冰箱");
            }
            food.setFridgeId(fridgeId);

            // 5. 存储到数据库
            foodMapper.add(food);
            log.info("食物添加成功: ID={}, 名称={}, 过期日期={}",
                    food.getId(), food.getName(), food.getExpiryDate());
        } catch (ExpirationWarningException e) {
            throw new FoodExpiredException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("添加食物失败: {}", e.getMessage());
            throw new FoodExpiredException("添加食物失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Food> getFoodList() {
        try {
            Integer fridgeId = fridgeMapper.selectByUserId(getCurrentUserId());
            List<Food> foods = foodMapper.getFoodList(fridgeId);

            // 检查食物状态
            foods.forEach(food -> {
                try {
                    expiryCheckService.checkAndWarnExpiration(food);
                } catch (ExpirationWarningException e) {
                    log.error("食物状态检查失败: ID={}", food.getId(), e);
                }
            });

            return foods;
        } catch (Exception e) {
            log.error("获取食物列表失败: {}", e.getMessage());
            throw new RuntimeException("获取食物列表失败", e);
        }
    }

    @Override
    public void deleteFood(List<Integer> ids) {
        try {
            Integer fridgeId = fridgeMapper.selectByUserId(getCurrentUserId());
            foodMapper.deleteFood(fridgeId, ids);
            log.info("删除食物成功: {}", ids);
        } catch (Exception e) {
            log.error("删除食物失败: {}", e.getMessage());
            throw new RuntimeException("删除食物失败", e);
        }
    }



    @Override
    @Transactional
    public void updateFood(Food food) throws FoodExpiredException {
        try {
            // 1. 更新时间
            food.setUpdateTime(LocalDateTime.now());

            // 2. 重新计算过期状态
            if (food.getManufactureDate() != null && food.getExpiryDuration() != null) {
                food.setExpiryDate(food.getManufactureDate().plusDays(food.getExpiryDuration()).toLocalDate());
                expiryCheckService.checkAndWarnExpiration(food);

                if (food.getExpired()) {
                    throw new FoodExpiredException("更新失败：食物已过期");
                }
            }

            // 3. 更新数据库
            foodMapper.updateFood(food);
            try {
                    log.info("开始更新食物过期状态...");
                    List<Food> foods = getFoodList();
                    expiryCheckService.batchCheckExpiration(foods);
                    log.info("食物过期状态更新完成，共处理{}个食物", foods.size());
                } catch (Exception e) {
                    log.error("更新食物过期状态失败: {}", e.getMessage());
                }

            log.info("食物更新成功: ID={}", food.getId());
        } catch (ExpirationWarningException e) {
            throw new FoodExpiredException("更新失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("更新食物失败: {}", e.getMessage());
            throw new FoodExpiredException("更新食物失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Food> getExpiringSoonFoods() {
        try {
            Integer fridgeId = fridgeMapper.selectByUserId(getCurrentUserId());
            return expiryCheckService.getExpiringSoonFoods(fridgeId);
        } catch (Exception e) {
            log.error("获取即将过期食物列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Food> getExpiredFoods() {
        try {
            Integer fridgeId = fridgeMapper.selectByUserId(getCurrentUserId());
            return expiryCheckService.getExpiredFoods(fridgeId);
        } catch (Exception e) {
            log.error("获取过期食物列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateFoodExpirationStatus() {
        try {
            log.info("开始更新食物过期状态...");
            List<Food> foods = getFoodList();
            expiryCheckService.batchCheckExpiration(foods);
            log.info("食物过期状态更新完成，共处理{}个食物", foods.size());
        } catch (Exception e) {
            log.error("更新食物过期状态失败: {}", e.getMessage());
        }
    }

    @Override
    public List<Food> getFoodsByExpiryStatus(String status) {
        try {
            List<Food> allFoods = getFoodList();
            return allFoods.stream()
                    .filter(food -> status.equals(food.getExpiryStatus()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据过期状态获取食物列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    private Integer getCurrentUserId() {
        return CurrentHold.getCurrentUserId();
    }
}