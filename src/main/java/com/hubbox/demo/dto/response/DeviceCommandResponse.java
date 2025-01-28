package com.hubbox.demo.dto.response;

public record DeviceCommandResponse(
    Long id,
    String modelId,
    String commandName,
    String commandTemplate,
    String description
) {
}
