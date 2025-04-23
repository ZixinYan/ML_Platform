package com.ml.aiservice.controller;

import com.ml.aiservice.config.MessageRedisSerializer;
import com.ml.aiservice.config.MyChatMemory;
import com.ml.aiservice.entity.ChatEntity;
import com.ml.aiservice.interceptor.LoginUserInterceptor;
import com.ml.common.utils.R;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.ml.aiservice.consts.MessageTypeEnum.USER;
import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/service")
@Slf4j
public class ChatController {
    private final ChatClient chatClient;

    @Autowired
    private final MessageRedisSerializer messageRedisSerializer;

    @Autowired
    private MyChatMemory myChatMemory;

    @Autowired
    public ChatController(ChatClient chatClient, MessageRedisSerializer messageRedisSerializer) {
        this.chatClient = chatClient;
        this.messageRedisSerializer = messageRedisSerializer;
    }

    /**
     * 创建一个新的对话
     */
    @PostMapping("/createChat")
    public R createChat() {
        try {
            // 初始化聊天信息，存储新的聊天记录
            String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());
            String chatId = UUID.randomUUID() + ":" + userId;
            return R.ok(chatId);
        } catch (Exception e) {
            log.error("新建对话失败: ", e);
            return R.error();
        }
    }

    /**
     * 使用某个 chatId 继续对话（流式返回）
     */
    @GetMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
        return R.ok(myChatMemory.get(chatId, 20));
    }

    /**
     * 清除会话
     */
    @DeleteMapping("/clearChat")
    public R clearChat(@RequestParam("chatId") String chatId) {
        myChatMemory.clear(chatId);
        return R.ok();
    }

}
