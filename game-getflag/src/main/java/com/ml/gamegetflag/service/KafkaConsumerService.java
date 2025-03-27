package com.ml.gamegetflag.service;

public interface KafkaConsumerService {

    void consumeMessage(String message);
}
