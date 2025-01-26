package com.hubbox.demo.dto.request;

import lombok.Builder;

@Builder
public record DeviceModelUpdateRequest(
    Long categoryId,
    String vendor,
    String description
) {
}
