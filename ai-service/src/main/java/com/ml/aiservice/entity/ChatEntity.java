package com.ml.aiservice.entity;

import com.ml.aiservice.consts.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatEntity implements Serializable {
    private String chatId;
    private MessageTypeEnum type;
    private String text;
}
