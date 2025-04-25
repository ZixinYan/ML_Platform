package com.ml.agent.memory;

import lombok.Getter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

@Getter
public class MessageWrapper {
    private String messageType;
    private String text;

    public MessageWrapper() {}

    public MessageWrapper(Message message) {
        this.text = message.getText();
        this.messageType = message.getMessageType().name(); // USER / ASSISTANT / SYSTEM
    }

    public Message toMessage() {
        return switch (messageType) {
            case "USER" -> new UserMessage(text);
            case "ASSISTANT" -> new AssistantMessage(text);
            case "SYSTEM" -> new SystemMessage(text);
            default -> throw new IllegalArgumentException("未知消息类型: " + messageType);
        };
    }
}
