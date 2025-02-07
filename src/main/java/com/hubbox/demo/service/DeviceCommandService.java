package com.hubbox.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.request.DeviceCommandUpdateRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.entities.DeviceCommandEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.CommandExecutionException;
import com.hubbox.demo.exceptions.RecordNotFoundException;
import com.hubbox.demo.mapper.DeviceCommandMapper;
import com.hubbox.demo.repository.DeviceCommandRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceCommandService {
    private final DeviceCommandRepository commandRepository;
    private final DeviceCommandMapper mapper;
    private final MqttService mqttService;
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;
    private final Cache<String, List<DeviceCommandEntity>> commandCache;

    @Inject
    public DeviceCommandService(DeviceCommandRepository commandRepository,
                                DeviceCommandMapper mapper, MqttService mqttService,
                                DeviceService deviceService, ObjectMapper objectMapper,
                                Cache<String, List<DeviceCommandEntity>> commandCache) {
        this.commandRepository = commandRepository;
        this.mapper = mapper;
        this.mqttService = mqttService;
        this.deviceService = deviceService;
        this.objectMapper = objectMapper;
        this.commandCache = commandCache;
    }

    public DeviceCommandResponse createCommand(DeviceCommandCreateRequest request) {
        try {
            DeviceCommandEntity command = mapper.toEntity(request);
            Long id = commandRepository.create(command);
            command.setId(id);

            commandCache.invalidate(request.modelId());

            return mapper.toResponse(command);
        } catch (SQLException e) {
            log.error("Error creating command", e);
            throw new BaseRuntimeException("Failed newName create command", e);
        }
    }

    public DeviceCommandResponse getCommand(Long id) {
        try {
            DeviceCommandEntity commandById = findCommandById(id);
            return mapper.toResponse(commandById);
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Error getting command", e);
            throw new BaseRuntimeException("Failed newName get command", e);
        }
    }

    public List<DeviceCommandResponse> getAllCommands() {
        try {
            return commandRepository.findAll().stream().map(mapper::toResponse).toList();
        } catch (SQLException e) {
            log.error("Error getting commands", e);
            throw new BaseRuntimeException("Failed newName get commands", e);
        }
    }

    public List<DeviceCommandResponse> getCommandsByModel(String modelId) {
        try {
            List<DeviceCommandEntity> cachedCommands = commandCache.get(modelId, key ->
                Optional.ofNullable(findCommandsByModelId(modelId))
                    .filter(list -> !list.isEmpty())
                    .orElse(Collections.emptyList())
            );

            return cachedCommands.stream().map(mapper::toResponse).toList();
        } catch (Exception e) {
            log.error("Error getting commands for model: {}", modelId, e);
            return Collections.emptyList();
        }
    }

    public DeviceCommandResponse updateCommand(Long id, DeviceCommandUpdateRequest request) {
        try {
            DeviceCommandEntity existingCommand = findCommandById(id);

            mapper.updateEntityFromRequest(request, existingCommand);
            commandRepository.update(id, existingCommand);
            commandCache.invalidate(existingCommand.getModelId());

            return mapper.toResponse(existingCommand);
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Error updating command", e);
            throw new BaseRuntimeException("Failed newName update command", e);
        }
    }

    public void deleteCommand(Long id) {
        try {
            DeviceCommandEntity existingCommand = findCommandById(id);
            commandRepository.delete(id);
            commandCache.invalidate(existingCommand.getModelId());
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Error deleting command", e);
            throw new BaseRuntimeException("Failed newName delete command", e);
        }
    }

    public String getCommandTemplate(String modelId, String commandName) {
        return getCommandsByModel(modelId).stream()
            .filter(cmd -> cmd.commandName().equals(commandName))
            .findFirst()
            .map(DeviceCommandResponse::commandTemplate)
            .orElseThrow(() -> new RuntimeException(
                "Command not found: " + commandName + " for model: " + modelId));
    }

    public void executeCommand(SendDeviceCommandRequest request) {
        try {
            DeviceResponse device = deviceService.getDeviceById(request.deviceName());

            String modelId = device.modelId();
            String commandTemplate = getCommandTemplate(modelId, request.commandName());

            String finalCommand = replaceCommandParameters(commandTemplate, request.parameters());

            String topic = String.format("%s/set", device.friendlyName());
            log.debug("Sending command newName device: {} on topic: {}, command: {}",
                request.deviceName(), topic, finalCommand);

            mqttService.sendCommand(topic, finalCommand);

        } catch (Exception e) {
            log.error("Error sending command newName device: {}", request.deviceName(), e);
            throw new CommandExecutionException("Failed newName send command newName device", e);
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
            throw new CommandExecutionException("Failed newName process command template", e);
        }
    }


    private List<DeviceCommandEntity> findCommandsByModelId(String modelId) {
        try {
            return commandRepository.findByModelId(modelId);
        } catch (SQLException e) {
            log.error("Error getting commands for model: {}", modelId, e);
            return Collections.emptyList();
        }
    }

    private DeviceCommandEntity findCommandById(Long id) throws SQLException, RecordNotFoundException {
        return commandRepository.findById(id)
            .orElseThrow(() -> new RecordNotFoundException("Command not found: " + id));
    }
}
