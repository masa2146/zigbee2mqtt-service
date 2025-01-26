package com.hubbox.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.listener.TopicMessageListener;
import java.util.ArrayList;
import java.util.List;

public class DeviceService implements TopicMessageListener {

    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final List<DeviceResponse> deviceList = new ArrayList<>();

    public DeviceService(MqttService mqttService, ObjectMapper objectMapper) {
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        subscribeToTopic();
    }

    public List<DeviceResponse> getAllDevices() {
        return deviceList;
    }

    public DeviceResponse getDeviceById(String deviceName) throws DeviceNotFoundException {
        return deviceList.stream()
            .filter(device -> device.friendlyName().equals(deviceName))
            .findFirst()
            .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
    }

    public void sendCommandToDevice(String deviceName, String command) throws DeviceNotFoundException {
        DeviceResponse device = getDeviceById(deviceName);
        mqttService.sendCommand("%s/set".formatted(device.friendlyName()), command);
    }

    @Override
    public void onMessage(String topic, String message) {
        deviceList.clear();
        try {
            List<DeviceResponse> updatedDevices = objectMapper.readValue(message,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DeviceResponse.class));
            deviceList.addAll(updatedDevices);
        } catch (JsonProcessingException e) {
            throw new BaseRuntimeException(e);
        }
    }

    private void subscribeToTopic() {
        mqttService.addTopicListener("bridge/devices", this);
    }

    public void shutdown() {
        mqttService.removeTopicListener("bridge/devices");
        deviceList.clear();
    }
}
