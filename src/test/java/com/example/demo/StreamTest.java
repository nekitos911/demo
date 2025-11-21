package com.example.demo;

import com.example.demo.configuration.OutputBridge;
import com.example.demo.dto.TestDto2;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
public class StreamTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectProvider<OutputBridge> testChannelOutput;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cloud.stream.bindings.testChannelOutput-out-0.destination}")
    private String testChannelOutTopic;

    @Test
    @SneakyThrows
    void testBridge() {
        var data = new TestDto2("test", Instant.now());
        testChannelOutput.ifAvailable(sender -> sender.send(data));

        Message<byte[]> receive = outputDestination.receive(1_000, testChannelOutTopic);

        var result = objectMapper.readValue(receive.getPayload(), TestDto2.class);

        assertThat(result)
                .isEqualTo(data);
    }
}
