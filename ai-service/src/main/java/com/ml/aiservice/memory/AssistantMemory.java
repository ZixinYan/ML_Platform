package com.ml.aiservice.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.aiservice.entity.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AssistantMemory implements ChatMemoryStore {

    @Value("${other.memory.mongo.assistant.key}")
    private String KEY_PREFIX;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String buildKey(Object id) {
        return KEY_PREFIX + id.toString();
    }


    @Override
    public List<ChatMessage> getMessages(Object chatId) {
        String key = buildKey(chatId);
        log.info("Fetching messages for chatId: {}", key);

        // Create a query with the criteria
        Criteria criteria = Criteria.where("chatId").is(key);
        Query query = new Query(criteria);

        // Query the database to find the matching document
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);

        if (chatMessages == null) {
            log.info("No messages found for key: {}", key);  // Log if no messages are found
            return Collections.emptyList();  // Return an empty list if no chat messages found
        }
        chatMessages.setLastUpdatedTime(new Date());
        mongoTemplate.save(chatMessages);

        String content = chatMessages.getContent();

        try {
            // Deserialize the content into a list of ChatMessages
            return ChatMessageDeserializer.messagesFromJson(content);
        } catch (Exception e) {
            log.error("Error deserializing messages for key: {}", chatId, e);  // Log the error
            return Collections.emptyList();  // Return an empty list in case of deserialization error
        }
    }

    @Override
    public void updateMessages(Object o, List<ChatMessage> list) {
        String key = buildKey(o);
        log.info("updateMessages key: {}, messages: {}", key, list);
        Criteria criteria = Criteria.where("chatId").is(key);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(list));
        update.set("lastUpdatedTime", new Date());
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }

    @Override
    public void deleteMessages(Object chatId) {
        String key = buildKey(chatId);
        Criteria criteria = Criteria.where("chatId").is(key);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }

    /**
     * 根据用户 ID 查找所有的 chatId
     * @param userId
     * @return
     */
    public List<String> findChatIdsByUserId(String userId) {
        String regex = KEY_PREFIX + userId + ".*";  // 正则表达式，匹配包含用户 ID 的 chatId

        // 构造查询条件
        Criteria criteria = Criteria.where("chatId").regex(regex);
        Query query = new Query(criteria);

        // 只查询 chatId 字段
        query.fields().include("chatId");

        // 执行查询并返回 chatId 列表
        List<ChatMessages> chatMessages = mongoTemplate.find(query, ChatMessages.class);
        return chatMessages.stream().map(ChatMessages::getChatId).collect(Collectors.toList());
    }

    // TTL
    public void createTTLIndex() {
        // 创建 TTL 索引，设置 lastUpdatedTime 字段在 10 天后过期
        Index index = new Index()
                .on("lastUpdatedTime", Sort.Direction.ASC)  // 设置按照 lastUpdatedTime 字段升序
                .expire(86400 * 10);  // 86400秒 * 10天，TTL 过期时间为 10 天

        // 为 ChatMessages 类创建索引
        mongoTemplate.indexOps(ChatMessages.class).ensureIndex(index);
    }


    @PostConstruct
    public void init() {
        createTTLIndex();
    }
}
