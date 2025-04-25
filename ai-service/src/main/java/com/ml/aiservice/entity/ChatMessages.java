package com.ml.aiservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {
    @Id
    private ObjectId messageId;

    private String chatId;

    private String content;

    // 更新时间字段，用于 TTL
    private Date lastUpdatedTime;
}
