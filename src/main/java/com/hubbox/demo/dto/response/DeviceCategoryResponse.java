package com.hubbox.demo.dto.response;

import lombok.Builder;

@Builder
public record DeviceCategoryResponse(
    Long id,
    String categoryName,
    String description) {
}
