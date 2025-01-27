package com.hubbox.demo.entities;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRuleEntity {
    private Long id;
    private String name;
    private String description;
    private RuleCondition condition;
    private RuleAction action;
}
