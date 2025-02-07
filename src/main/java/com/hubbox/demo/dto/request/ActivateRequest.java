package com.hubbox.demo.dto.request;

public record ActivateRequest(
    String pinNumber,
    Boolean activate
) {
}
