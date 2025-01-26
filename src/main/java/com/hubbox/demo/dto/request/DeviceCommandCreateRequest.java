package com.hubbox.demo.dto.request;

public record DeviceCommandCreateRequest(
//    @NotBlank(message = "Model ID cannot be empty")
    String modelId,
//    @NotBlank(message = "Command name cannot be empty")
    String commandName,
//    @NotBlank(message = "Command template cannot be empty")
    String commandTemplate,
    String description
) {
}
