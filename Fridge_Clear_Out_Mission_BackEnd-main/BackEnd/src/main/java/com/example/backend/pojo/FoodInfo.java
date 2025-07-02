package com.example.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodInfo {
    private String name;
    private Integer number;
    private LocalDateTime manufactureDate; // 生产日期
    private Integer expiryDuration;// 保质期（天数）
    public LocalDateTime expiryDate;//
}