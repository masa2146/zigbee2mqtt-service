package com.hubbox.demo.dto;

import lombok.Builder;

@Builder
public record DeviceCriteria(
    String deviceId,
    String field,
    ComparisonOperator operator,
    Object value
) {
}
