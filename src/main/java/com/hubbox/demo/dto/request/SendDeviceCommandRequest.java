package com.hubbox.demo.dto.request;

import java.util.Map;
import lombok.Builder;

@Builder
public record SendDeviceCommandRequest(
    String deviceName,
    String commandName,
    Map<String, Object> parameters
) {
}
