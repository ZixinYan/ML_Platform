package com.ml.agent.controller;

import com.ml.agent.interceptor.LoginUserInterceptor;
import com.ml.agent.memory.MessageRedisSerializer;
import com.ml.agent.memory.AgentMemory;
import com.ml.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 基于SpringAi实现一个陪伴沟通
 * 实现了基于聊天id的记忆隔离
 */

@RestController
@RequestMapping("/agent")
@Slf4j
public class ChatController {
    private final ChatClient chatClient;

    @Autowired
    private final MessageRedisSerializer messageRedisSerializer;

    @Autowired
    private AgentMemory agentMemory;

    @Autowired
    public ChatController(ChatClient chatClient, MessageRedisSerializer messageRedisSerializer) {
        this.chatClient = chatClient;
        this.messageRedisSerializer = messageRedisSerializer;
    }

    /**
     * 创建一个新的对话
     */
    @PostMapping("/newChat")
    public R createChat() {
        try {
            // 初始化聊天信息，存储新的聊天记录
            String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());
            String chatId = userId + ":" + UUID.randomUUID();
            return R.ok().setData(chatId);
        } catch (Exception e) {
            log.error("新建对话失败: ", e);
            return R.error();
        }
    }

    /**
     * 使用某个 chatId 继续对话（流式返回）
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> chatStream(
            @RequestParam("chatId") String chatId,
            @RequestParam("message") String message) {
        try {
            Flux<String> response = chatClient.prompt()
                    .user(message)
                    .advisors(advisor -> advisor
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .stream().content();
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取历史记录
     */
    @GetMapping("/getMessages")
    public R getMessages(@RequestParam("chatId") String chatId) {
        return R.ok(agentMemory.get(chatId, 20));
    }


    /**
     * 获取全部历史记录
     */
    @GetMapping("/getAllMessages")
    public R getAllMessages() {
        String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());
        return R.ok(agentMemory.getAllByUserId(userId));
    }


    /**
     * 清除会话
     */
    @DeleteMapping("/clear")
    public R clearChat(@RequestParam("chatId") String chatId) {
        agentMemory.clear(chatId);
        return R.ok();
    }

}
