package com.hubbox.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.CommandExecutionException;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.listener.TopicMessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceService implements TopicMessageListener {
    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final DeviceCommandService commandService;
    private final List<DeviceResponse> deviceList = new ArrayList<>();

    @Inject
    public DeviceService(MqttService mqttService, ObjectMapper objectMapper, DeviceCommandService commandService) {
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        this.commandService = commandService;
        initialize();
    }

    private void initialize() {
        subscribeToTopic();
        log.info("Device service initialized");
    }

    public void sendCommandToDevice(SendDeviceCommandRequest request) {
        try {
            DeviceResponse device = getDeviceById(request.deviceName());

            String modelId = device.modelId();
            String commandTemplate = commandService.getCommandTemplate(modelId, request.commandName());

            String finalCommand = replaceCommandParameters(commandTemplate, request.parameters());

            String topic = String.format("%s/set", device.friendlyName());
            log.debug("Sending command to device: {} on topic: {}, command: {}",
                device.friendlyName(), topic, finalCommand);

            mqttService.sendCommand(topic, finalCommand);

        } catch (Exception e) {
            log.error("Error sending command to device: {}", request.deviceName(), e);
            throw new CommandExecutionException("Failed to send command to device", e);
        }
    }

    private String replaceCommandParameters(String template, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return template;
        }

        try {
            JsonNode templateNode = objectMapper.readTree(template);
            ObjectNode commandNode = templateNode.deepCopy();

            parameters.forEach((key, value) -> {
                if (commandNode.has(key)) {
                    if (value instanceof Number numVal) {
                        commandNode.put(key, (numVal.doubleValue()));
                    } else if (value instanceof Boolean boolVal) {
                        commandNode.put(key, boolVal);
                    } else {
                        commandNode.put(key, value.toString());
                    }
                }
            });

            return objectMapper.writeValueAsString(commandNode);
        } catch (JsonProcessingException e) {
            log.error("Error processing command template", e);
            throw new CommandExecutionException("Failed to process command template", e);
        }
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

    private void subscribeToTopic() {
        mqttService.addTopicListener("bridge/devices", this);
        log.info("Subscribed to bridge/devices topic");
    }

    public void shutdown() {
        mqttService.removeTopicListener("bridge/devices");
        synchronized (deviceList) {
            deviceList.clear();
        }
        log.info("Device service shut down");
    }
}
