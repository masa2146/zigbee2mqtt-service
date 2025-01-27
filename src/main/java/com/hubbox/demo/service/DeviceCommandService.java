package com.hubbox.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
import com.hubbox.demo.entities.DeviceCommandEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.mapper.DeviceCommandMapper;
import com.hubbox.demo.repository.DeviceCommandRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceCommandService {
    private final DeviceCommandRepository commandRepository;
    private final DeviceCommandMapper mapper;
    private final Cache<String, List<DeviceCommandEntity>> commandCache;

    @Inject
    public DeviceCommandService(DeviceCommandRepository commandRepository, DeviceCommandMapper mapper,
                                Cache<String, List<DeviceCommandEntity>> commandCache) {
        this.commandRepository = commandRepository;
        this.mapper = mapper;
        this.commandCache = commandCache;
    }


    public DeviceCommandResponse createCommand(DeviceCommandCreateRequest request) {
        try {

            DeviceCommandEntity command = mapper.toEntity(request);
            Long id = commandRepository.create(command);
            command.setId(id);

            // Invalidate cache
            commandCache.invalidate(request.modelId());

            return mapper.toResponse(command);
        } catch (SQLException e) {
            log.error("Error creating command", e);
            throw new BaseRuntimeException("Failed to create command", e);
        }
    }

    public List<DeviceCommandResponse> getCommandsByModel(String modelId) {
        try {
            return Objects.requireNonNull(commandCache.get(modelId, key -> getCommandsFromRepository(modelId))).stream()
                .map(mapper::toResponse).toList();
        } catch (Exception e) {
            log.error("Error getting commands for model: {}", modelId, e);
            return Collections.emptyList();
        }
    }

    private List<DeviceCommandEntity> getCommandsFromRepository(String modelId) {
        try {
            return commandRepository.findByModelId(modelId);
        } catch (SQLException e) {
            log.error("Error getting commands for model: {}", modelId, e);
            return Collections.emptyList();
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
}
