package com.hubbox.demo.dto;

import java.util.List;

public record RuleCondition(
    LogicalOperator operator,
    List<DeviceCriteria> criteria
) {
}
