package com.ml.aiservice.config;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ChatModelConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModelName;

    @Bean(name = "deepseek-chat")
    public OpenAiStreamingChatModel deepseekChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(openAiApiKey)
                .baseUrl(openAiBaseUrl)
                .modelName(openAiModelName)
                .temperature(1.5)
                .build();
    }

    @Value("${langchain4j.community.dashscope.streaming-chat-model.api-key}")
    private String qwenApiKey;

    @Value("${langchain4j.community.dashscope.streaming-chat-model.base-url}")
    private String qwenBaseUrl;

    @Value("${langchain4j.community.dashscope.streaming-chat-model.model-name}")
    private String qwenModelName;

    @Bean(name = "qwen")
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(qwenApiKey)
                .modelName(qwenModelName)
                .temperature(1.5F)
                .repetitionPenalty(1.2F)
                .build();
    }


}
