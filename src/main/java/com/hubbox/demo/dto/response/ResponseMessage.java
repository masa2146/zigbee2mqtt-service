package com.hubbox.demo.dto.response;

public record ResponseMessage(
     int status,
     String error,
     String message
) {
}
