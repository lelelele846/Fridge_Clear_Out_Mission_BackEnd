package com.example.backend.controller;

import com.example.backend.pojo.Result;
import com.example.backend.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReportController
 * 负责处理与"报告"相关的所有HTTP请求。
 * 目前主要用于生成和查看食物保鲜报告。
 */
@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {

    // 注入ReportService，负责报告的业务逻辑
    @Autowired
    private ReportService reportService;

    /**
     * 生成并返回当前用户的食物保鲜报告。
     * 当收到 GET /reports/freshness 请求时，此方法被调用。
     *
     * @return Result 封装的报告内容（通常为AI生成的文本报告）
     */
    @GetMapping("/freshness")
    public Result getFreshnessReport() {
        log.info("开始生成食物保鲜报告...");
        String report = reportService.generateFreshnessReport();
        log.info("食物保鲜报告生成完毕。");
        return Result.success(report);
    }
}