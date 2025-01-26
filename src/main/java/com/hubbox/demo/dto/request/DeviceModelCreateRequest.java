package com.hubbox.demo.dto.request;

import lombok.Builder;

@Builder
public record DeviceModelCreateRequest(
//    @NotNull(message = "Category id cannot be null")
    Long categoryId,
//    @NotEmpty(message = "Model id cannot be empty")
    String modelId,
//    @NotEmpty(message = "Vendor cannot be empty")
    String vendor,
    String description
) {
}
