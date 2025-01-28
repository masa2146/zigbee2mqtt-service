package com.hubbox.demo.dto.response;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;

public record DeviceRuleResponse(
    Long id,
    String name,
    String description,
    RuleCondition condition,
    RuleAction action
) {
}
