package com.ml.gamegetflag.service;

public interface KafkaProducerService {

    void sendMessage(String message);
}
