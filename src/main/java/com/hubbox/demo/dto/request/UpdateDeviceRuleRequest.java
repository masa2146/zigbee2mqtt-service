package com.hubbox.demo.dto.request;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;

public record UpdateDeviceRuleRequest(
    String name,
    String description,
    RuleCondition condition,
    RuleAction action,
    Boolean isActive
) {
}
