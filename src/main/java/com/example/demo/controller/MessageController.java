package com.example.demo.controller;

import com.example.demo.configuration.OutputBridge;
import com.example.demo.dto.TestDto;
import com.example.demo.dto.TestDto4;
import com.example.demo.dto.TestDto5;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MessageController {

    private final ObjectProvider<OutputBridge> produceMessageTopic23;
    private final ObjectProvider<OutputBridge> produceMessageTopic4Dlq;
    private final ObjectProvider<OutputBridge> produceMessageTopic4;
    private final ObjectProvider<OutputBridge> produceMessageTopic5;
    private final ObjectProvider<OutputBridge> produceMessageTopic6Native;
    private final ObjectProvider<OutputBridge> produceMessageTopic7Native;

    @PostMapping("/send-error")
    void sendMessageError(@RequestBody TestDto message) {
        produceMessageTopic4Dlq.ifAvailable(sender -> sender.send(message));
    }

    @PostMapping("/send")
    void sendMessage(@RequestBody TestDto message) {
        produceMessageTopic23.ifAvailable(sender -> sender.send(message));
    }

    @PostMapping("/send-demo4")
    void sendMessageDemo4(@RequestBody TestDto4 message) {
        produceMessageTopic4.ifAvailable(sender -> sender.send(message));
    }

    @PostMapping("/send-demo5")
    void sendMessageDemo5(@RequestBody TestDto5 message) {
        produceMessageTopic5.ifAvailable(sender -> sender.send(message));
    }

    @PostMapping("/send-native6")
    void sendMessageNative6(@RequestBody TestDto message) {
        produceMessageTopic6Native.ifAvailable(sender -> sender.send(message));
    }

    @PostMapping("/send-native7")
    void sendMessageNative7(@RequestBody TestDto message) {
        produceMessageTopic7Native.ifAvailable(sender -> sender.send(message));
    }
}
