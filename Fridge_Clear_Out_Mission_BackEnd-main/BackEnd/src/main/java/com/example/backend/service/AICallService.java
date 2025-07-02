package com.example.backend.service;

import com.example.backend.exception.AIException;
import com.example.backend.pojo.Food;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AICallService {

    Food getFoodNutrition(String foodName) throws AIException;

    Food getFoodInfoByImage(MultipartFile file) throws Exception;

    String generateFreshnessReport(List<Food> foodList);
}
