package com.hubbox.demo.dto.request;

public record DeviceCommandUpdateRequest(
    String commandName,
    String commandTemplate,
    String description
) {
}
