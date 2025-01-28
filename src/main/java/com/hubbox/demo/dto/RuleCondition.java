package com.hubbox.demo.dto;

import java.util.List;

public record RuleCondition(
    List<DeviceCriteria> criteria,
    Long maxTimeDifferenceMs, // Maksimum zaman farkı (milisaniye cinsinden)
    List<String> requiredDeviceSequence // Sıralı cihaz listesi
) {
}
