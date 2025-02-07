package com.hubbox.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.DeviceCreateRequest;
import com.hubbox.demo.dto.request.DeviceRenameRequest;
import com.hubbox.demo.dto.request.DeviceUpdateRequest;
import com.hubbox.demo.dto.request.PermitRequest;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.dto.response.ResponseMessage;
import com.hubbox.demo.entities.DeviceEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.exceptions.RecordNotFoundException;
import com.hubbox.demo.listener.TopicMessageListener;
import com.hubbox.demo.mapper.DeviceMapper;
import com.hubbox.demo.repository.DeviceRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceService implements TopicMessageListener, AutoCloseable {
    private static final String DEVICE_TOPIC = "bridge/devices";
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final MqttService mqttService;
    private final SensorEventManager eventManager;
    private final ObjectMapper objectMapper;
    private final List<DeviceResponse> deviceList = new ArrayList<>();

    @Inject
    public DeviceService(DeviceRepository deviceRepository, DeviceMapper deviceMapper, MqttService mqttService,
                         SensorEventManager eventManager,
                         ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
        this.mqttService = mqttService;
        this.eventManager = eventManager;
        this.objectMapper = objectMapper;
        initialize();
    }

    private void initialize() {
        subscribeToTopic();
        log.info("Device service initialized");
    }

    public DeviceResponse createDevice(DeviceCreateRequest device) {
        try {
            DeviceEntity entity = deviceMapper.toEntity(device);
            this.deviceRepository.create(entity);
            DeviceResponse response = deviceMapper.toResponse(entity);
            synchronized (deviceList) {
                deviceList.add(response);
            }
            return response;
        } catch (Exception e) {
            log.error("Error creating device", e);
            throw new BaseRuntimeException(e);
        }

    }

    public DeviceResponse updateDevice(Long id, DeviceUpdateRequest device) {
        try {
            DeviceEntity entity = findDeviceById(id);
            removeDeviceFromList(entity);
            deviceMapper.updateEntityFromRequest(device, entity);
            this.deviceRepository.update(id, entity);
            DeviceResponse response = deviceMapper.toResponse(entity);
            synchronized (deviceList) {
                deviceList.add(response);
            }
            return response;
        } catch (Exception e) {
            log.error("Error updating device", e);
            throw new BaseRuntimeException(e);
        }
    }

    public void deleteDevice(Long id) {
        try {
            DeviceEntity entity = findDeviceById(id);
            removeDeviceFromList(entity);
            this.deviceRepository.delete(id);
        } catch (Exception e) {
            log.error("Error deleting device", e);
            throw new BaseRuntimeException(e);
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

    public ResponseMessage renameDevice(DeviceRenameRequest request) {
        synchronized (deviceList) {
            deviceList.stream()
                .filter(device -> device.friendlyName().equals(request.oldName()))
                .findFirst()
                .ifPresent(device -> mqttService.sendCommand(DEVICE_TOPIC + "/rename", request.toJson().toJSONString()));
        }
        return new ResponseMessage(200, null, "Device renamed successfully");
    }

    public ResponseMessage permitAll() {
        mqttService.sendCommand(DEVICE_TOPIC + "/bridge/request/permit_join", new PermitRequest(true).toJson().toJSONString());
        return new ResponseMessage(200, null, "Permit all devices");
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
                deviceRepository.findAll().stream().map(deviceMapper::toResponse).forEach(deviceList::add);
                deviceList.addAll(updatedDevices);
            }

            log.debug("Updated device list, total devices: {}", deviceList.size());
        } catch (JsonProcessingException | SQLException e) {
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

    private DeviceEntity findDeviceById(Long id) throws SQLException, RecordNotFoundException {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new RecordNotFoundException("Device not found: " + id));
    }

    private void removeDeviceFromList(DeviceEntity entity) {
        synchronized (deviceList) {
            deviceList.stream().filter(deviceResponse -> deviceResponse.friendlyName().equals(entity.getFriendlyName())).findFirst()
                .ifPresent(deviceList::remove);
        }
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
