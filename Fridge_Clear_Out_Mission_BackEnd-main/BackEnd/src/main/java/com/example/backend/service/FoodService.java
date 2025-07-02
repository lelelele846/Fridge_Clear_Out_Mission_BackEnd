package com.example.backend.service;

import com.example.backend.pojo.Food;

import java.util.List;


public interface FoodService {
        void add(Food food);
        List<Food> getFoodList();
        void deleteFood(List<Integer> ids);
        void updateFood(Food food);
        List<Food> getExpiringSoonFoods();
        List<Food> getExpiredFoods();
        void updateFoodExpirationStatus();
        List<Food> getFoodsByExpiryStatus(String status);
    }

