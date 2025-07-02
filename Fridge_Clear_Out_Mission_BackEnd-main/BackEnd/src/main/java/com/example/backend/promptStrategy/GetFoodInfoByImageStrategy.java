package com.example.backend.promptStrategy;

import com.example.backend.pojo.Food;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetFoodInfoByImageStrategy implements PromptStrategy {
    @Override
    public String buildPrompt(Object... params) {
        return  "你是一位营养学家，请识别这张图片中的食物是什么，并严格按照以下格式返回食物的名称(不要生成其他内容):\n" +
                "食物名称:xx";
    }

    @Override
    public Object parseContent(String content, Object... params) {
        log.info("Content:" + content);
        String trimContent = content.trim();
            if (trimContent.startsWith("食物名称:")) {
                String foodName = trimContent.substring(5);
                log.info("foodName: " + foodName);
                return foodName;
            }

        return null;
    }

    @Override
    public Object buildMessage(Object... params) {
        String imageBase64 = (String) params[0];
        //log.info("imageBase64: " + imageBase64);
        //log.info("buildmassage");
        Map<String, Object> imageMessage = new HashMap<>();
        imageMessage.put("role", "user");
        imageMessage.put("content",imageBase64);
        imageMessage.put("contentType", "image");
        Map<String, Object> promptMessage = new HashMap<>();
        promptMessage.put("role", "user");
        promptMessage.put("content", buildPrompt());
        promptMessage.put("contentType", "text");
        Object message = new Object[]{
                imageMessage,
                promptMessage
        };
       //log.info("Message: " + message.toString());
        return message;
    }
}
