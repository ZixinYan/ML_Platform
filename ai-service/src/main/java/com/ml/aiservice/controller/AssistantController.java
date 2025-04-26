package com.ml.aiservice.controller;


import com.ml.aiservice.Serivce.AssistantService;
import com.ml.aiservice.Serivce.impl.AssistantServiceImpl;
import com.ml.aiservice.assistant.Assistant;
import com.ml.aiservice.dto.ChatHistoryDto;
import com.ml.aiservice.interceptor.LoginUserInterceptor;
import com.ml.aiservice.memory.AssistantMemory;
import com.ml.aiservice.utils.MessageUtils;
import com.ml.common.utils.R;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.ml.aiservice.utils.MessageUtils.extractText;

@RestController
@Slf4j
@RequestMapping("/service/assistant")
@RequiredArgsConstructor
public class AssistantController {

    @Autowired
    private Assistant assistant;

    @Autowired
    private AssistantMemory assistantMemory;

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 创建一个新的对话
     */
    @PostMapping("/newChat")
    public R createChat() {
        try {
            // 初始化聊天信息，存储新的聊天记录
            String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());
            String chatId = userId + ":" + UUID.randomUUID();
            return R.ok("success",chatId);
        } catch (Exception e) {
            log.error("新建对话失败: ", e);
            return R.error();
        }
    }

    /**
     * 继续一次聊天对话（流式返回响应内容）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam("chatId") String chatId, @RequestParam("message") String message) {
        String userName = LoginUserInterceptor.loginUser.get().getNickname();
        String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());
        return assistant.chat(chatId, message, userName, userId);
    }


    /**
     * 获取聊天记录
     */
    @GetMapping("/history")
    public R getHistory(@RequestParam String chatId) {
        List<ChatMessage> messages = assistantMemory.getMessages(chatId);
        List<ChatHistoryDto.ChatMessageDto> dtoList = new ArrayList<>();

        for (ChatMessage message : messages) {
            String role = "unknown";
            String content = null;
            if (message instanceof UserMessage userMsg) {
                role = "user";
                if (!userMsg.contents().isEmpty()) {
                    content = extractText(userMsg.contents().get(0).toString());
                }
            } else if (message instanceof AiMessage aiMsg) {
                role = "assistant";
                content = extractText(aiMsg.toString());
            } else {
                continue;
            }

            dtoList.add(new ChatHistoryDto.ChatMessageDto(role, content));

        }

        ChatHistoryDto chatHistoryDto = new ChatHistoryDto();
        chatHistoryDto.setMessages(dtoList);
        return R.ok("success", chatHistoryDto);
    }

    /**
     * 所有聊天记录
     */
    @GetMapping("/all")
    public R getAllHistory() {
        List<String> chatIds = assistantMemory.findChatIdsByUserId(String.valueOf(LoginUserInterceptor.loginUser.get().getId()));
        return R.ok("success", chatIds);
    }


    /**
     * 删除聊天记录
     */
    @DeleteMapping("/delete")
    public R deleteChat(@RequestParam String chatId) {
        try {
            assistantMemory.deleteMessages(chatId);
            return R.ok("删除成功");
        } catch (Exception e) {
            log.error("删除聊天记录失败: ", e);
            return R.error();
        }
    }

    /**
     * 上傳文件到向量數據庫
     */
    @PostMapping("/upload")
    public R uploadFiles(@RequestParam("files") MultipartFile[] multipartFiles) {
        try {
            File[] files = Arrays.stream(multipartFiles)
                    .map(multipartFile -> {
                        try {
                            String random = String.valueOf(UUID.randomUUID());
                            File tempFile = File.createTempFile(random, multipartFile.getOriginalFilename());
                            multipartFile.transferTo(tempFile);
                            return tempFile;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(File[]::new);

            assistantService.uploadKnowledgeLibrary(files);
            return R.ok("文件已成功上传并入库！");
        } catch (Exception e) {
            log.error(e.toString());
            return R.error();
        }
    }
}
