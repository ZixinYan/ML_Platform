package com.ml.aichat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ml.aichat.config.StoryWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaConsumerServiceImpl {

    @Autowired
    private StoryWebSocketHandler storyWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @KafkaListener(topics = "${kafka.topic.response}", groupId = "chat-group")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String flag = null;
            JsonNode response = objectMapper.readTree(record.value());
            log.info(response.toString());
            String requestId = response.get("request_id").asText();
            String generatedText = response.get("response").asText();
            JsonNode flagNode = response.get("flag");
            if (flagNode != null) {
                 flag = flagNode.asText();
                log.info("Flag: " + flag);
            }
            if (flag != null) {
                // **保证 Redis 仅存储一次**
                boolean isStored = Boolean.TRUE.equals(redisTemplate.opsForValue()
                        .setIfAbsent(requestId, flag, 30, TimeUnit.MINUTES));
                if (isStored) {
                    log.info("成功存入 Redis: key={}, flag={}", requestId, flag);
                } else {
                    log.info("requestId={} 的 flag 已存在，不重复存入", requestId);
                }
            }

            ((ObjectNode) response).remove("flag");
            log.info(generatedText);
            log.info(requestId);
            String jsonMessage = objectMapper.writeValueAsString(response);
            storyWebSocketHandler.sendMessageToAll(jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
