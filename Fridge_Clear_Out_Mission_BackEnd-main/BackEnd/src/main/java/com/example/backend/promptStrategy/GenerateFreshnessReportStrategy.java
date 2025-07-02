package com.example.backend.promptStrategy;

import com.example.backend.pojo.Food;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * GenerateFreshnessReportStrategy
 * 用于生成食物保鲜报告的AI提示词（Prompt）
 * 实现 PromptStrategy 接口，负责将食物列表转为结构化的分析指令
 */
@Component
public class GenerateFreshnessReportStrategy implements PromptStrategy {

    /**
     * 构建用于AI生成保鲜报告的Prompt
     * @param params 期望传入 List<Food> 作为唯一参数
     * @return String 结构化的Prompt文本
     */
    @Override
    public String buildPrompt(Object... params) {
        if (params.length == 0 || !(params[0] instanceof List)) {
            throw new IllegalArgumentException("请传入食物列表 List<Food> 作为参数");
        }
        @SuppressWarnings("unchecked")
        List<Food> foods = (List<Food>) params[0];
        StringBuilder sb = new StringBuilder();
        sb.append("你是一名专业的智能冰箱管家和营养顾问。你的任务是分析用户冰箱中的食物清单，并生成一份简洁、有洞察力的中文食物保鲜报告。\n");
        sb.append("报告的目的是帮助用户了解自己的食物存储习惯，并提供实用的建议以减少浪费。\n\n");
        sb.append("以下是用户冰箱中的食物数据，数据格式为：\n");
        sb.append("[\n");
        for (Food food : foods) {
            sb.append(String.format("  {\"name\": \"%s\", ", food.getName()));
            sb.append(String.format("\"expiryDate\": \"%s\", ", food.getExpiryDate() != null ? food.getExpiryDate() : "未知"));
            sb.append(String.format("\"number\": %d, ", food.getNumber() != null ? food.getNumber() : 1));
            sb.append(String.format("\"createTime\": \"%s\"", food.getCreateTime() != null ? food.getCreateTime().toLocalDate() : "未知"));
            sb.append("},\n");
        }
        sb.append("]\n\n");
        sb.append("请根据上述食物数据，严格按照以下结构和要求生成报告，报告全文使用中文：\n\n");
        sb.append("1. 总体评价：用一两句精炼的话总结当前冰箱的整体存储状态。\n");
        sb.append("2. 【做得好的地方】：分析并列出1到2个用户做得好的存储习惯。\n");
        sb.append("3. 【可以改进的地方】：分析并列出1到2个最值得关注的问题点，必须具体到食物名称。\n");
        sb.append("4. 【本周饮食小贴士】：根据即将过期的食物，给出一个简单、具体、可操作的食谱或饮食建议。\n");
        sb.append("风格应亲切、鼓励，避免批评。\n");
        sb.append("请开始分析并生成报告。\n");
        return sb.toString();
    }

    /**
     * 解析AI返回的报告内容（本功能直接返回文本，无需结构化解析）
     * @param content AI返回的报告文本
     * @param params  可选参数
     * @return String 直接返回报告文本
     */
    @Override
    public Object parseContent(String content, Object... params) {
        return content;
    }

    @Override
    public Object buildMessage(Object... params) {
        return null;
    }
}