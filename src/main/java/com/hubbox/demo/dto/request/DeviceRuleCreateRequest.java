package com.hubbox.demo.dto.request;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;

public record DeviceRuleCreateRequest(
//    @NotBlank
    String name,
    String description,
//        @NotNull
    RuleCondition condition,
//        @NotNull
    RuleAction action
) {
}
