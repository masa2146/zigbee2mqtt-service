package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.DeviceCreateRequest;
import com.hubbox.demo.dto.request.DeviceUpdateRequest;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.entities.DeviceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceMapper extends BaseMapper<DeviceCreateRequest, DeviceUpdateRequest, DeviceEntity, DeviceResponse> {
    DeviceMapper INSTANCE = Mappers.getMapper(DeviceMapper.class);
}
