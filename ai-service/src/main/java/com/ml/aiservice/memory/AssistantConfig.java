package com.ml.aiservice.memory;


import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantConfig {
    @Autowired
    private AssistantMemory assistantMemory;

    @Bean(name = "assistantMemoryProvider")
    public ChatMemoryProvider assistantMemoryProvider(){
        return chatId -> MessageWindowChatMemory.builder()
                .id(chatId)
                .maxMessages(100)
                .chatMemoryStore(assistantMemory)
                .build();
    }
}
