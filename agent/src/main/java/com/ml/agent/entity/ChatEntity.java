package com.ml.agent.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ml.agent.consts.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ChatEntity implements Serializable {
    private String chatId;
    private MessageTypeEnum type;
    private String text;
}
