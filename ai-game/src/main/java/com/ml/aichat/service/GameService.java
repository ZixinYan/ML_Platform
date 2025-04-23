package com.ml.aichat.service;

public interface GameService {
    boolean generate(String prompt,String requestId);

    boolean success(String requestId, String flag);
}
