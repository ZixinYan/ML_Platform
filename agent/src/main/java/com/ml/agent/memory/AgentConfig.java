package com.ml.agent.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AgentConfig {
    @Autowired
    private AgentMemory agentMemory;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你现在是用户的青梅竹马兼女朋友，你的性格比较腹黑，请用戏谑的语气回答用户的问题")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(agentMemory),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
