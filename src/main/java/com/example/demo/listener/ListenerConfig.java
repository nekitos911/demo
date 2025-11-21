package com.example.demo.listener;

import com.example.demo.dto.TestDto;
import com.example.demo.dto.TestDto2;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class ListenerConfig {

    @Bean
    public Consumer<Message<TestDto>> consumeMessageTopic4Dlq() {
        return message -> {
            log.info("Received message. Message: {}", message);
            if (true) {
                throw new RuntimeException("test");
            }
        };
    }

    @Bean
    public Consumer<Message<TestDto2>> consumeMessageTopic1() {
        return message -> {
            log.info("Received message. Message: {}", message);
        };
    }

    @Bean
    public Function<Message<TestDto>, Message<TestDto2>> consumeMultiplyTopicsAndProduceMessageTopic2() {
        return message -> {
            log.info("Received message. Message: {}", message);
            if (message.getPayload().resend()) {
                return MessageBuilder.withPayload(new TestDto2(message.getPayload().name(), message.getPayload().instant()))
                        .setHeader("test", "test")
                        .build();
            }
            return null;
        };
    }

    @Bean
    public Consumer<Message<JsonNode>> consumeMultiplyTopicsWithDifferentData() {
        return msg -> {
            String topic = msg.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC, String.class);
            log.info("Received message. Topic: {}", topic);
        };
    }

    @Bean
    public Consumer<Message<TestDto>> consumeMessageTopic6Native() {
        return msg -> {
            log.info("Received message. Message: {}", msg);
        };
    }

    @Bean
    public Consumer<Message<TestDto>> consumeMessageTopic7() {
        return msg -> {
            log.info("Received message. Message: {}", msg);
        };
    }

}
