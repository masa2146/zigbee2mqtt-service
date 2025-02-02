package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.request.DeviceRuleCreateRequest;
import com.hubbox.demo.dto.request.DeviceRuleUpdateRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.response.DeviceRuleResponse;
import com.hubbox.demo.entities.DeviceRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceRuleMapper
    extends BaseMapper<DeviceRuleCreateRequest, DeviceRuleUpdateRequest, DeviceRuleEntity, DeviceRuleResponse> {
    DeviceRuleMapper INSTANCE = Mappers.getMapper(DeviceRuleMapper.class);

    @Mapping(target = "deviceName", source = "targetDeviceName")
    SendDeviceCommandRequest toDeviceCommandRequest(RuleAction action);
}
