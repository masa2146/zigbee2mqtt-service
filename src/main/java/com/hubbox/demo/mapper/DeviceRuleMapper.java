package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.request.CreateDeviceRuleRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.request.UpdateDeviceRuleRequest;
import com.hubbox.demo.dto.response.DeviceRuleResponse;
import com.hubbox.demo.entities.DeviceRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DeviceRuleMapper {
    DeviceRuleMapper INSTANCE = Mappers.getMapper(DeviceRuleMapper.class);

    DeviceRuleResponse toResponse(DeviceRuleEntity rule);

    DeviceRuleEntity toEntity(CreateDeviceRuleRequest request);

    void updateEntityFromRequest(UpdateDeviceRuleRequest request, @MappingTarget DeviceRuleEntity rule);

    @Mapping(target = "deviceName", source = "targetDeviceId")
    SendDeviceCommandRequest toDeviceCommandRequest(RuleAction action);
}
