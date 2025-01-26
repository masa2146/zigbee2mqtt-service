package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.request.DeviceModelUpdateRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
import com.hubbox.demo.entities.DeviceCommand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceCommandMapper {
    DeviceCommandMapper INSTANCE = Mappers.getMapper(DeviceCommandMapper.class);

    DeviceCommandResponse toResponse(DeviceCommand command);
    DeviceCommand toEntity(DeviceCommandCreateRequest request);
    void updateEntityFromRequest(DeviceModelUpdateRequest request, @MappingTarget DeviceCommand command);
}
