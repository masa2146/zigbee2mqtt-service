package com.hubbox.demo.dto;

import lombok.Builder;

@Builder
public record DeviceCriteria(
    String deviceName,
    String field,
    ComparisonOperator operator,
    Object value
) {
}
