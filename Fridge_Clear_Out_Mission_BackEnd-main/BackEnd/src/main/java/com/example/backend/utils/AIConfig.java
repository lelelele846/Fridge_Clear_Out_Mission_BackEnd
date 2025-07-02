package com.example.backend.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vivo.ai")
@Data
public class AIConfig {
    private String appId;
    private String appKey;
    private String modelName;
    private String imageModelName;
    private double temperature;
    private int maxTokens;
}
