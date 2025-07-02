package com.example.backend.controller;

import com.example.backend.exception.AIException;
import com.example.backend.exception.ExpirationWarningException;
import com.example.backend.exception.FoodExpiredException;
import com.example.backend.pojo.Food;
import com.example.backend.pojo.FoodInfo;
import com.example.backend.pojo.Result;
import com.example.backend.service.AICallService;
import com.example.backend.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private AICallService aiCallService;

    @PostMapping
    public Result addFood(@RequestBody FoodInfo foodInfo) {
        try {
            // 校验
            if (foodInfo.getManufactureDate() == null) {
                return Result.error(400, "生产日期不能为空");
            }
            if (foodInfo.getExpiryDuration() == null || foodInfo.getExpiryDuration() <= 0) {
                return Result.error(400, "保质期必须大于0");
            }
            // 只用AI获取营养信息
            Food food = aiCallService.getFoodNutrition(foodInfo.getName());
            // 强制覆盖AI的保质期和生产日期
            food.setManufactureDate(foodInfo.getManufactureDate());
            food.setExpiryDuration(foodInfo.getExpiryDuration());
            food.setNumber(foodInfo.getNumber());
            food.setName(foodInfo.getName());
            foodService.add(food);
            return Result.success(food);
        } catch (AIException e) {
            log.error("获取食物营养信息失败: {}", e.getMessage());
            return Result.error(500, "获取食物信息失败: " + e.getMessage());
        } catch (FoodExpiredException e) {
            log.warn("拒绝添加过期食物: {}", foodInfo.getName());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("添加食物异常: {}", e.getMessage());
            return Result.error(500, "系统错误");
        }
    }

    @GetMapping
    public Result getFoodList() {
        try {
            List<Food> foods = foodService.getFoodList();
            return Result.success(foods);
        } catch (Exception e) {
            log.error("获取食物列表失败: {}", e.getMessage());
            return Result.error(500, "获取食物列表失败");
        }
    }

    @PostMapping("/delete")
    public Result deleteFood(@RequestParam List<Integer> ids) {
        try {
            foodService.deleteFood(ids);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除食物失败: {}", e.getMessage());
            return Result.error(500, "删除失败");
        }
    }

    @PutMapping("/{id}")
    public Result updateFood(@PathVariable Integer id, @RequestBody Food food) {
        try {
            food.setId(id);
            foodService.updateFood(food);
            return Result.success("更新成功");
        } catch (FoodExpiredException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新食物失败: {}", e.getMessage());
            return Result.error(500, "更新失败");
        }
    }

    // 新增过期管理相关接口

    @GetMapping("/expiring-soon")
    public Result getExpiringSoonFoods() {
        try {
            List<Food> foods = foodService.getExpiringSoonFoods();
            return Result.success(foods);
        } catch (Exception e) {
            log.error("获取即将过期食物列表失败: {}", e.getMessage());
            return Result.error(500, "获取即将过期食物列表失败");
        }
    }

    @GetMapping("/expired")
    public Result getExpiredFoods() {
        try {
            List<Food> foods = foodService.getExpiredFoods();
            return Result.success(foods);
        } catch (Exception e) {
            log.error("获取过期食物列表失败: {}", e.getMessage());
            return Result.error(500, "获取过期食物列表失败");
        }
    }

    @PostMapping("/update-expiration-status")
    public Result updateExpirationStatus() {
        try {
            foodService.updateFoodExpirationStatus();
            return Result.success("过期状态更新成功");
        } catch (Exception e) {
            log.error("更新过期状态失败: {}", e.getMessage());
            return Result.error(500, "更新过期状态失败");
        }
    }

    @GetMapping("/by-status/{status}")
    public Result getFoodsByStatus(@PathVariable String status) {
        try {
            List<Food> foods = foodService.getFoodsByExpiryStatus(status);
            return Result.success(foods);
        } catch (Exception e) {
            log.error("根据状态获取食物列表失败: {}", e.getMessage());
            return Result.error(500, "获取食物列表失败");
        }
    }

    @GetMapping("/expiration-info")
    public Result getExpirationInfo() {
        try {
            List<Food> allFoods = foodService.getFoodList();
            List<Food> expiringSoonFoods = foodService.getExpiringSoonFoods();
            List<Food> expiredFoods = foodService.getExpiredFoods();

            return Result.success(new ExpirationInfo(
                    allFoods.size(),
                    expiringSoonFoods.size(),
                    expiredFoods.size(),
                    expiringSoonFoods,
                    expiredFoods
            ));
        } catch (Exception e) {
            log.error("获取过期信息失败: {}", e.getMessage());
            return Result.error(500, "获取过期信息失败");
        }
    }

    // 内部类用于返回过期信息
    public static class ExpirationInfo {
        private int totalFoods;
        private int expiringSoonCount;
        private int expiredCount;
        private List<Food> expiringSoonFoods;
        private List<Food> expiredFoods;

        public ExpirationInfo(int totalFoods, int expiringSoonCount, int expiredCount,
                              List<Food> expiringSoonFoods, List<Food> expiredFoods) {
            this.totalFoods = totalFoods;
            this.expiringSoonCount = expiringSoonCount;
            this.expiredCount = expiredCount;
            this.expiringSoonFoods = expiringSoonFoods;
            this.expiredFoods = expiredFoods;
        }

        // Getters
        public int getTotalFoods() { return totalFoods; }
        public int getExpiringSoonCount() { return expiringSoonCount; }
        public int getExpiredCount() { return expiredCount; }
        public List<Food> getExpiringSoonFoods() { return expiringSoonFoods; }
        public List<Food> getExpiredFoods() { return expiredFoods; }
    }
}