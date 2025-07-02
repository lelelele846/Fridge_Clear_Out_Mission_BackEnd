package com.example.backend.service.impl;

import com.example.backend.pojo.Food;
import com.example.backend.service.FoodService;
import com.example.backend.service.ReportService;
import com.example.backend.service.AICallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ReportServiceImpl
 * 负责实现 ReportService 接口，生成各类报告。
 * 目前实现生成食物保鲜报告的功能。
 */
@Service
public class ReportServiceImpl implements ReportService {

    // 注入 FoodService，用于获取当前用户冰箱中的所有食物信息
    @Autowired
    private FoodService foodService;

    // 注入 AI 服务，用于调用 AI 生成报告
    @Autowired
    private AICallService aiCallService;

    /**
     * 生成当前用户的食物保鲜报告。
     * 1. 获取所有食物信息
     * 2. 调用 AI 服务生成报告
     * 3. 返回报告文本
     *
     * @return String 由AI生成的报告文本
     */
    @Override
    public String generateFreshnessReport() {
        // 第一步：获取当前用户冰箱中的所有食物信息
        List<Food> foodList = foodService.getFoodList();

        // 第二步：调用 AI 服务生成报告
        // 这里假设 AICallService 有 generateFreshnessReport 方法，实际项目中需补充实现
        // 你可以在 AICallServiceImpl 里实现该方法，调用对应的 PromptStrategy
        String report = aiCallService.generateFreshnessReport(foodList);

        // 第三步：返回报告文本
        return report;
    }
}