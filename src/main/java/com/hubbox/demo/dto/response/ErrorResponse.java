package com.hubbox.demo.dto.response;

public record ErrorResponse(
     int status,
     String error,
     String message
) {
}
