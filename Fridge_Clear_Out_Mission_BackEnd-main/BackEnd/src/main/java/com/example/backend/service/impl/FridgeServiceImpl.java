package com.example.backend.service.impl;

import com.example.backend.mapper.FridgeMapper;
import com.example.backend.pojo.Fridge;
import com.example.backend.service.FridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FridgeServiceImpl implements FridgeService {
    @Autowired
    private FridgeMapper fridgeMapper;
    @Override
    public void add(Integer id) {
        Fridge fridge = new Fridge();
        fridge.setUserId(id);
        fridge.setCreateTime(LocalDateTime.now());
        fridgeMapper.add(fridge);
    }
}
