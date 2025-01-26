package com.hubbox.demo.dto.request;

import lombok.Builder;

@Builder
public record DeviceCategoryUpdateRequest(
//    @NotEmpty(message = "Category name cannot be empty")
    String categoryName,
    String description
) {
}
