package com.hubbox.demo.dto;

import java.util.Map;

public record DeviceDataSnapshot(
    String deviceName,
    Map<String, Object> data,
    Long timestamp
) {
}
