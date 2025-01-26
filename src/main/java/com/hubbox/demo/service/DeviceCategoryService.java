package com.hubbox.demo.service;

import com.hubbox.demo.dto.request.DeviceCategoryCreateRequest;
import com.hubbox.demo.dto.response.DeviceCategoryResponse;
import com.hubbox.demo.entities.DeviceCategory;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.mapper.DeviceCategoryMapper;
import com.hubbox.demo.repository.DeviceCategoryRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeviceCategoryService {
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceCategoryMapper mapper;


    public DeviceCategoryResponse createCategory(DeviceCategoryCreateRequest request) {
        try {
            DeviceCategory category = mapper.toEntity(request);
            Long id = categoryRepository.create(category);
            category.setId(id);
            return mapper.toResponse(category);
        } catch (SQLException e) {
            log.error("Error creating category", e);
            throw new BaseRuntimeException("Failed to create category", e);
        }
    }

    public DeviceCategoryResponse getCategory(Long id) {
        try {
            return categoryRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        } catch (SQLException e) {
            log.error("Error getting category", e);
            throw new BaseRuntimeException("Failed to get category", e);
        }
    }

    public List<DeviceCategoryResponse> getAllCategories() {
        try {
            return categoryRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            log.error("Error getting categories", e);
            throw new BaseRuntimeException("Failed to get categories", e);
        }
    }
}
