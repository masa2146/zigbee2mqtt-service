package com.hubbox.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.listener.TopicMessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceService implements TopicMessageListener, AutoCloseable {
    private static final String DEVICE_TOPIC = "bridge/devices";
    private final MqttService mqttService;
    private final SensorEventManager eventManager;
    private final ObjectMapper objectMapper;
    private final List<DeviceResponse> deviceList = new ArrayList<>();

    @Inject
    public DeviceService(MqttService mqttService, SensorEventManager eventManager,
                         ObjectMapper objectMapper) {
        this.mqttService = mqttService;
        this.eventManager = eventManager;
        this.objectMapper = objectMapper;
        initialize();
    }

    private void initialize() {
        subscribeToTopic();
        log.info("Device service initialized");
    }

    public List<DeviceResponse> getAllDevices() {
        return deviceList;
    }

    public DeviceResponse getDeviceById(String deviceName) throws DeviceNotFoundException {
        return deviceList.stream()
            .filter(device -> device.friendlyName().equals(deviceName))
            .findFirst()
            .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceName));
    }

    @Override
    public void onMessage(String topic, String message) {
        if (topic.equals(DEVICE_TOPIC)) {
            loadDevices(message);
        } else {
            checkSensorData(topic, message);
        }

    }

    private void loadDevices(String message) {
        try {
            List<DeviceResponse> updatedDevices = objectMapper.readValue(message,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DeviceResponse.class));

            synchronized (deviceList) {
                deviceList.clear();
                deviceList.addAll(updatedDevices);
            }

            log.debug("Updated device list, total devices: {}", deviceList.size());
        } catch (JsonProcessingException e) {
            log.error("Error processing device update message", e);
            throw new BaseRuntimeException(e);
        }
    }

    private void checkSensorData(String topic, String message) {
        deviceList.stream()
            .filter(device -> device.friendlyName().equals(topic))
            .findFirst()
            .ifPresent(device -> {
                try {
                    Map<String, Object> deviceData = objectMapper.readValue(message, new TypeReference<>() {
                    });
                    eventManager.publishEvent(topic, deviceData);
                } catch (JsonProcessingException e) {
                    log.error("Error parsing device data for {}: {}", topic, message, e);
                }
            });
    }

    private void subscribeToTopic() {
        mqttService.addTopicListener(DEVICE_TOPIC, this);
        mqttService.addTopicListener("#", this);
    }


    @Override
    public void close() throws Exception {
        mqttService.removeTopicListener(DEVICE_TOPIC);
        mqttService.removeTopicListener("#");
        synchronized (deviceList) {
            deviceList.clear();
        }
        log.info("Device service shut down");
    }
}
