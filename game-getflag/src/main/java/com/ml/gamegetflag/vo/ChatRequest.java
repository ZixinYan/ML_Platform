package com.ml.gamegetflag.vo;

import lombok.Data;

@Data
public class ChatRequest {
    private Long userId;
    private String taskId;
    private String prompt;
    private String model;
}
