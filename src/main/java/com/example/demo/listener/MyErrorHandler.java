package com.example.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyErrorHandler {

    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(ErrorMessage message) {
        log.warn("BEFORE DLQ: {}", message);
    }
}