package com.ml.aiservice.assistant;


import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatMemoryProvider = "assistantMemoryProvider",
        streamingChatModel = "qwen",
        tools = "tools",
        contentRetriever = "contentRetriever"
)
public interface Assistant {
    @SystemMessage(fromResource = "assistant_prompt.txt")
    Flux<String> chat(@MemoryId String chatId, @UserMessage String message, @V("userName") String userName, @V("userId") String userID);
}
