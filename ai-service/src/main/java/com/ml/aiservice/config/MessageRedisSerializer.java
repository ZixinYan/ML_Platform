package com.ml.aiservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
public class MessageRedisSerializer implements RedisSerializer<Message> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Message message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    @Override
    public Message deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(bytes);
            String type = node.get("messageType").asText(); // "USER" / "ASSISTANT" / "SYSTEM"
            String text = node.get("text").asText();

            return switch (type) {
                case "USER" -> new UserMessage(text);
                case "ASSISTANT" -> new AssistantMessage(text);
                case "SYSTEM" -> new SystemMessage(text);
                default -> throw new IllegalArgumentException("未知消息类型: " + type);
            };
        } catch (Exception e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
