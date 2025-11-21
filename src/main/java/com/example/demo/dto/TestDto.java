package com.example.demo.dto;

import java.time.Instant;

public record TestDto(String name, Instant instant, boolean resend) {
}
