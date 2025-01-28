package com.hubbox.demo.dto;

import java.util.Map;

public record DeviceDataSnapshot(
    String deviceId,
    Map<String, Object> data,
    Long timestamp
) {
}
