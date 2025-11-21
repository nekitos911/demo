package com.example.demo.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class OutputBridge {

    private final StreamBridge bridge;

    public <T> void send(T payload) {
        send(payload, Map.of());
    }

    public <T> void send(T payload, Map<String, Object> additionalHeaders) {
        var headers = new HashMap<>(additionalHeaders);
        if (payload instanceof Message<?> msg) {
            headers.put("Content-Type", "application/json");
            headers.put(KafkaHeaders.KEY, msg.getId());
        }

        for (String channel : channels()) {
            log.info("Sending to {}: {}", channel, payload);
            bridge.send(channel, MessageBuilder.createMessage(payload, new MessageHeaders(headers)));
        }
    }

    public abstract List<String> channels();
}
