package com.example.backend.service;

/**
 * ReportService
 * 负责与报告生成相关的业务逻辑接口定义。
 * 目前主要用于生成食物保鲜报告，后续可扩展更多类型的报告。
 */
public interface ReportService {

    /**
     * 生成当前用户的食物保鲜报告。
     * 该方法会收集用户冰箱中的所有食物信息，
     * 并调用AI服务生成一份详细的保鲜分析报告。
     *
     * @return String 由AI生成的报告文本，内容为中文。
     */
    String generateFreshnessReport();

    // 未来可扩展：
    // String generateConsumptionReport(); // 生成消耗报告
    // String generateNutritionReport();   // 生成营养报告
}