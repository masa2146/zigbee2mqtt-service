package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.request.DeviceCommandUpdateRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
import com.hubbox.demo.entities.DeviceCommandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceCommandMapper
    extends BaseMapper<DeviceCommandCreateRequest, DeviceCommandUpdateRequest, DeviceCommandEntity, DeviceCommandResponse> {
    DeviceCommandMapper INSTANCE = Mappers.getMapper(DeviceCommandMapper.class);
}
