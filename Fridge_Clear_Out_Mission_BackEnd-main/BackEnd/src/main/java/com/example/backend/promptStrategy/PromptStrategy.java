package com.example.backend.promptStrategy;

public interface PromptStrategy {
    String buildPrompt(Object... params);
    Object parseContent(String content, Object... params);
    Object buildMessage(Object... params);
}