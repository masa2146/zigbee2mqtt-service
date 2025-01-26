package com.hubbox.demo.dto.response;

import java.util.List;

public record DeviceModelResponse(
    Long id,
    String modelId,
    String vendor,
    String description,
    DeviceCategoryResponse category,
    List<DeviceCommandResponse> commands
    ) {
}
