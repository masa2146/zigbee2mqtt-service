package com.hubbox.demo.service;

import com.hubbox.demo.dto.request.DeviceModelCreateRequest;
import com.hubbox.demo.dto.response.DeviceModelResponse;
import com.hubbox.demo.entities.DeviceModel;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.mapper.DeviceModelMapper;
import com.hubbox.demo.repository.DeviceModelRepository;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeviceModelService {
    private final DeviceModelRepository modelRepository;
    private final DeviceModelMapper mapper;
    private final DeviceCategoryService categoryService;

    public DeviceModelResponse createModel(DeviceModelCreateRequest request) {
        try {
            // Category exists check
            categoryService.getCategory(request.categoryId());

            DeviceModel model = mapper.toEntity(request);
            Long id = modelRepository.create(model);
            model.setId(id);
            return mapper.toResponse(model);
        } catch (SQLException e) {
            log.error("Error creating model", e);
            throw new BaseRuntimeException("Failed to create model", e);
        }
    }

    public DeviceModelResponse getModelByModelId(String modelId) {
        try {
            return modelRepository.findByModelId(modelId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Model not found: " + modelId));
        } catch (SQLException e) {
            log.error("Error getting model", e);
            throw new BaseRuntimeException("Failed to get model", e);
        }
    }
}
