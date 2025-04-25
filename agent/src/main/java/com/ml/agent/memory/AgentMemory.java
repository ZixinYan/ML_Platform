package com.ml.agent.memory;

import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.agent.consts.MessageTypeEnum;
import com.ml.agent.entity.ChatEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class AgentMemory implements ChatMemory {

    @Value("${other.memory.redis.springAI.key}")
    private String KEY_PREFIX;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    public AgentMemory(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("add conversationId: {} messages: {}", conversationId, messages);

        String key = KEY_PREFIX + conversationId;
        int MAX_HISTORY = 100;

        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > MAX_HISTORY) {
            redisTemplate.opsForList().trim(key, size - MAX_HISTORY, -1);
        }

        for (Message msg : messages) {
            try {
                // 添加 messageType 字段，确保反序列化时能识别
                String json = objectMapper.writeValueAsString(new MessageWrapper(msg));
                redisTemplate.opsForList().rightPush(key, json);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize message: {}", msg, e);
            }
        }

        redisTemplate.expire(key, 10, TimeUnit.DAYS);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = KEY_PREFIX + conversationId;

        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory instanceof LettuceConnectionFactory lettuce) {
            System.out.println("连接 Redis Host: " + lettuce.getHostName());
            System.out.println("连接 Redis Port: " + lettuce.getPort());
            System.out.println("连接 Redis DB: " + lettuce.getDatabase());
        }


        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        log.info(key);
        redisTemplate.expire(key, 10, TimeUnit.DAYS);
        int start = Math.max(0, (int) (size - lastN));

        List<Object> listTmp = redisTemplate.opsForList().range(key, start, -1);
        List<Message> listOut = new ArrayList<>();
        for (Object jsonStr : listTmp) {
            try {
                String jsonString = (String) jsonStr;
                MessageWrapper wrapper = objectMapper.readValue(jsonString, MessageWrapper.class);
                listOut.add(wrapper.toMessage());
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize message: {}", jsonStr, e);
            }
        }

        return listOut;
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(KEY_PREFIX + conversationId);
    }


    public List<List<Message>> getAllByUserId(String userId) {
        String pattern = KEY_PREFIX + userId + ":*";
        List<List<Message>> result = new ArrayList<>();

        redisTemplate.execute((RedisConnection connection) -> {
            Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                            .match(pattern.getBytes(StandardCharsets.UTF_8)) // 使用字节数组匹配模式
                            .count(1000)
                            .build()
            );
            while (cursor.hasNext()) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                log.info("SCAN命中 key：{}", key);

                List<Object> listTmp = redisTemplate.opsForList().range(key, 0, -1);
                if (listTmp == null) continue;

                List<Message> listOut = new ArrayList<>();
                for (Object jsonStr : listTmp) {
                    try {
                        String jsonString = (String) jsonStr;
                        MessageWrapper wrapper = objectMapper.readValue(jsonString, MessageWrapper.class);
                        listOut.add(wrapper.toMessage());
                    } catch (JsonProcessingException e) {
                        log.error("反序列化失败，key: {}, value: {}", key, jsonStr, e);
                    }
                }

                result.add(listOut);
            }
            return null;
        });

        return result;
    }


}
