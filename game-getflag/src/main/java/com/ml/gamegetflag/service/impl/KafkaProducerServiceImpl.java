package com.ml.gamegetflag.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerServiceImpl implements com.ml.gamegetflag.service.KafkaProducerService {

    @Value("${kafka.topic.request}")
    private String REQUEST_TOPIC;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        kafkaTemplate.send(REQUEST_TOPIC, message);
    }
}
