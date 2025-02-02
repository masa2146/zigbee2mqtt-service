package com.hubbox.demo.dto;

import java.util.Map;

public record RuleAction(
    String targetDeviceName,
    String commandName,
    Map<String, Object> parameters
) {
}
