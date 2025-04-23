package com.ml.aichat.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.R;
import com.ml.aichat.service.impl.GameServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameServiceImpl gameService;


    @Value("${ollama.url}")
    private String ollamaUrl;

    @Autowired
    private WebClient webClient;


    // 与Ollama进行交互，流式传输获取数据
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithOllama(@RequestParam String prompt) {
        String requestBody = String.format("{\"model\": \"mistral\", \"prompt\": \"%s\", \"stream\": true}", prompt);

        return webClient.post()
                .uri(ollamaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(response -> {
                    try {
                        // 使用 fastjson 解析 JSON 字符串
                        JSONObject jsonResponse = JSON.parseObject(response);
                        String message = jsonResponse.getString("response"); // 提取 "response" 字段
                        return Mono.just(message); // 将提取的 "response" 字段作为结果返回
                    } catch (Exception e) {
                        return Mono.error(e); // 如果解析出错，返回错误
                    }
                });
    }

    @PostMapping("/story")
    public R generate(@RequestParam String prompt) {
        String requestId = UUID.randomUUID().toString();
        if(gameService.generate(prompt,requestId)) {
            return R.ok("requestId", requestId);
        }else{
            return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/success")
    public R success(@RequestParam String requestId, @RequestParam String flag) {
        flag = "flag{" + flag + "}";
        if(gameService.success(requestId,flag)) {
            return R.ok();
        }else{
            return R.error();
        }
    }


}
