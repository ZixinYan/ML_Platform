package com.ml.aiservice.controller;


import com.ml.aiservice.Serivce.AssistantService;
import com.ml.aiservice.assistant.Assistant;
import com.ml.aiservice.dto.ChatHistoryDto;
import com.ml.aiservice.interceptor.LoginUserInterceptor;
import com.ml.aiservice.memory.AssistantMemory;
import com.ml.common.utils.R;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.servlet.http.HttpServlet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ml.aiservice.utils.MessageUtils.extractText;

@RestController
@Slf4j
@RequestMapping("/service/assistant")
@RequiredArgsConstructor
public class AssistantController extends HttpServlet {

    @Autowired
    private Assistant assistant;

    @Autowired
    private AssistantMemory assistantMemory;

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private Scheduler assistantScheduler;

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
    public Flux<String> chat(@RequestParam("chatId") String chatId,
                             @RequestParam("message") String message) {
        String userName = LoginUserInterceptor.loginUser.get().getNickname();
        String userId = String.valueOf(LoginUserInterceptor.loginUser.get().getId());

        return Mono.fromCallable(() -> assistant.chat(chatId, message, userName, userId))
                .subscribeOn(assistantScheduler)
                .flatMapMany(Function.identity());
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history")
    public Mono<R<ChatHistoryDto>> getHistory(@RequestParam String chatId) {
        return Mono.fromCallable(() -> {
            List<ChatMessage> messages = assistantMemory.getMessages(chatId);
            List<ChatHistoryDto.ChatMessageDto> dtoList = messages.stream().map(message -> {
                String role = "unknown";
                String content = null;
                if (message instanceof UserMessage userMsg && !userMsg.contents().isEmpty()) {
                    role = "user";
                    content = extractText(userMsg.contents().get(0).toString());
                } else if (message instanceof AiMessage aiMsg) {
                    role = "assistant";
                    content = extractText(aiMsg.toString());
                }
                return new ChatHistoryDto.ChatMessageDto(role, content);
            }).filter(msg -> msg.getContent() != null).collect(Collectors.toList());

            ChatHistoryDto chatHistoryDto = new ChatHistoryDto();
            chatHistoryDto.setMessages(dtoList);
            return R.ok("success", chatHistoryDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 所有聊天记录
     */
    @GetMapping("/all")
    public Mono<R<List<String>>> getAllHistory() {
        return Mono.fromCallable(() -> {
            List<String> chatIds = assistantMemory.findChatIdsByUserId(String.valueOf(LoginUserInterceptor.loginUser.get().getId()));
            return R.ok("success", chatIds);
        }).subscribeOn(Schedulers.boundedElastic());
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
    public Mono<R<Object>> uploadFiles(@RequestParam("files") MultipartFile[] multipartFiles) {
        return Mono.fromCallable(() -> {
                    File[] files = Arrays.stream(multipartFiles)
                            .map(multipartFile -> {
                                try {
                                    File tempFile = File.createTempFile(UUID.randomUUID().toString(), multipartFile.getOriginalFilename());
                                    multipartFile.transferTo(tempFile);
                                    return tempFile;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).toArray(File[]::new);
                    assistantService.uploadKnowledgeLibrary(files);
                    return R.ok("success");
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("文件已成功上传并入库！: ", e);
                    return Mono.just(R.error());
                });
    }
}
