package com.example.backend.service.impl;

import com.example.backend.exception.AIException;
import com.example.backend.pojo.Food;
import com.example.backend.promptStrategy.GenerateFreshnessReportStrategy;
import com.example.backend.promptStrategy.GetFoodInfoByImageStrategy;
import com.example.backend.promptStrategy.GetFoodNutritionByNameStrategy;
import com.example.backend.promptStrategy.PromptStrategy;
import com.example.backend.service.AICallService;
import com.example.backend.utils.AIConfig;
import com.example.backend.utils.AliyunOSSOperator;
import com.example.backend.utils.VivoAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Slf4j
@Service
public class AICallServiceImpl implements AICallService {
    private static final String API_URL = "https://api-ai.vivo.com.cn/vivogpt/completions";
    private static final String API_PATH = "/vivogpt/completions";
    private final RestTemplate restTemplate;
    private AIConfig aiConfig;
    @Autowired
    private AliyunOSSOperator ossOperator;
    private PromptStrategy generateFreshnessReportStrategy;

    @Autowired
    public AICallServiceImpl(RestTemplate restTemplate, AIConfig aiConfig) {
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
        this.generateFreshnessReportStrategy = new GenerateFreshnessReportStrategy();
    }

    @Override
    public Food getFoodNutrition(String foodName) throws AIException {
       PromptStrategy promptStrategy = new GetFoodNutritionByNameStrategy();
       Object[] params = new Object[]{foodName};
        Food food = (Food) GetInfoFromAI(promptStrategy, params);
        food.setName(foodName);
        return food;
    }

    @Override
    public Food getFoodInfoByImage(MultipartFile file) throws Exception {
        PromptStrategy promptStrategy = new GetFoodInfoByImageStrategy();
        String base64Image = convertToBase64(file);
        Object[] params = new Object[]{base64Image};
        String foodName = (String) GetInfoFromAI(promptStrategy, params);
        log.info("food: " + foodName);
        Food food = getFoodNutrition(foodName);
        food.setName(foodName);
        return food;
    }

    @Override
    public String generateFreshnessReport(List<Food> foods) {
        PromptStrategy promptStrategy = this.generateFreshnessReportStrategy;
        Object[] params = new Object[]{foods};
        return (String) GetInfoFromAI(promptStrategy, params);
    }

    public static String convertToBase64(MultipartFile file) throws Exception {
        // 获取文件的 MIME 类型
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        // 确定文件格式对应的前缀
        String formatPrefix = getFormatPrefix(contentType);

        // 读取文件内容并进行 Base64 编码
        byte[] fileContent = file.getBytes();
        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);

        // 返回完整的 Base64 字符串（包含数据前缀）
        return formatPrefix + base64Encoded;
    }

    private static String getFormatPrefix(String contentType) {
        if (contentType.startsWith("image/jpeg")) {
            return "data:image/JPEG;base64,";
        } else if (contentType.startsWith("image/png")) {
            return "data:image/PNG;base64,";
        } else if (contentType.startsWith("image/gif")) {
            return "data:image/GIF;base64,";
        } else {
            // 默认处理其他类型
            return "data:" + contentType + ";base64,";
        }
    }

    private Object GetInfoFromAI(PromptStrategy promptStrategy,Object... params) throws AIException {
        try {
            UUID requestId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            log.info("requestId: " + requestId);
            //产生查询参数
            Map<String,Object> queryParams = new HashMap<>();
            queryParams.put("requestId", requestId.toString());
            String queryStr = mapToQueryString(queryParams);
            //产生请求体
            Map<String,Object> requestBody;
            if(promptStrategy instanceof GetFoodInfoByImageStrategy){
                log.info("GetFoodInfoByImageStrategy");
                requestBody = buildRequestBodyByImageStrategy(promptStrategy,params);
                log.info("requestBody: {}", requestBody);
            }
            else{
                requestBody = buildRequestBody(promptStrategy, sessionId.toString(), params);
            }
            //产生请求头
            HttpHeaders headers = null;
            try {
                headers = VivoAuth.generateAuthHeaders(
                        aiConfig.getAppId(),
                        aiConfig.getAppKey(),
                        "POST",
                        API_PATH,
                        queryStr
                );
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            //拼接完整请求URL
            String url = String.format("http://api-ai.vivo.com.cn%s?%s", API_PATH, queryStr);
            //发送请求
            HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(requestBody,headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("response: " + response.getBody());
            //解析响应
            return parseResponse(promptStrategy,response,params);
        } catch (Exception e) {
            throw new AIException("AI调用失败" + params, e);
        }
    }
    private Map<String,Object> buildRequestBodyByImageStrategy(PromptStrategy promptStrategy,Object... params) throws AIException {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("model",aiConfig.getImageModelName());
        log.info("model: " + aiConfig.getImageModelName());
        requestBody.put("sessionId",UUID.randomUUID().toString());
        log.info("sessionId: " + requestBody.get("sessionId"));
        requestBody.put("messages",promptStrategy.buildMessage(params));
        log.info("requestBody In BuildRequestBody: {}", requestBody);
        return requestBody;
    }
    //构建请求体
    private Map<String, Object> buildRequestBody(PromptStrategy promptStrategy,String sessionId,Object... params) {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("prompt",promptStrategy.buildPrompt(params));
        log.info("model: " + aiConfig.getModelName());
        requestBody.put("model",aiConfig.getModelName());
        requestBody.put("sessionId",sessionId.toString());
        requestBody.put("extra",buildExtraParam());
        return requestBody;
    }
    //构建extra参数
    private Object buildExtraParam() {
        Map<String,Object> extra = new HashMap<>();
        extra.put("temperature",aiConfig.getTemperature());
        extra.put("max_new_tokens",aiConfig.getMaxTokens());
        return extra;
    }

    //解析响应
    private Object parseResponse(PromptStrategy promptStrategy,ResponseEntity<String> response, Object... params) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new AIException("AI调用失败,状态码:" + response.getStatusCode() + " .错误信息: " + response.getBody());
        }

        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new AIException("AI调用失败,响应体为空");
        }

        try {
            // 解析JSON响应，提取content字段
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 检查code是否为0（成功）
            int code = rootNode.path("code").asInt();
            if (code != 0) {
                String msg = rootNode.path("msg").asText("未知错误");
                throw new AIException("AI调用失败,错误码:" + code + ", 错误信息: " + msg);
            }

            // 提取content字段
            String content = rootNode.path("data").path("content").asText();
            if (content.isEmpty()) {
                throw new AIException("AI返回内容为空");
            }

            return  promptStrategy.parseContent(content,params);
        } catch (JsonProcessingException e) {
            throw new AIException("解析AI响应失败", e);
        }
    }
    //将map转换为查询字符串
    private static String mapToQueryString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }
        return queryString.toString();
    }

}
