package com.hubbox.demo.dto.request;

public record DeviceUpdateRequest(
    Boolean disabled,
    String friendlyName,
    String modelId
) {
}
