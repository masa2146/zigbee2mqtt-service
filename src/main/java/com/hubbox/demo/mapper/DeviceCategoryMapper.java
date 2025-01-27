package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.DeviceCategoryCreateRequest;
import com.hubbox.demo.dto.request.DeviceCategoryUpdateRequest;
import com.hubbox.demo.dto.response.DeviceCategoryResponse;
import com.hubbox.demo.entities.DeviceCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceCategoryMapper {
    DeviceCategoryMapper INSTANCE = Mappers.getMapper(DeviceCategoryMapper.class);

    DeviceCategoryResponse toResponse(DeviceCategoryEntity category);
    DeviceCategoryEntity toEntity(DeviceCategoryCreateRequest request);
    void updateEntityFromRequest(DeviceCategoryUpdateRequest request, @MappingTarget DeviceCategoryEntity category);
}
