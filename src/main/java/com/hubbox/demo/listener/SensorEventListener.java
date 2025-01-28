package com.hubbox.demo.listener;

import java.util.Map;

public interface SensorEventListener {
    void onDeviceDataReceived(String deviceId, Map<String, Object> data);
}
