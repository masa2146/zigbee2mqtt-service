package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.DeviceModelCreateRequest;
import com.hubbox.demo.dto.request.DeviceModelUpdateRequest;
import com.hubbox.demo.dto.response.DeviceModelResponse;
import com.hubbox.demo.entities.DeviceModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceModelMapper {
    DeviceModelMapper INSTANCE = Mappers.getMapper(DeviceModelMapper.class);

    DeviceModelResponse toResponse(DeviceModel model);
    DeviceModel toEntity(DeviceModelCreateRequest request);
    void updateEntityFromRequest(DeviceModelUpdateRequest request, @MappingTarget DeviceModel model);
}
