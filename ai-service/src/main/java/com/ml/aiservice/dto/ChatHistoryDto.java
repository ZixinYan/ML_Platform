package com.ml.aiservice.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryDto {
    private List<ChatMessageDto> messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessageDto {
        private String role;
        private String content;

        public static ChatMessageDto from(String role, String content) {
            return new ChatMessageDto(role, content);
        }
    }

    public ChatHistoryDto fromString(String chatHistoryJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(chatHistoryJson, ChatHistoryDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ChatHistoryDto();
    }
}
