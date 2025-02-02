package com.hubbox.demo.dto.request;

public record DeviceCreateRequest(
    Boolean disabled,
    String friendlyName,
    String modelId
) {
}
