package com.hubbox.demo.mapper;

import com.hubbox.demo.dto.request.PinCreateRequest;
import com.hubbox.demo.dto.request.PinUpdateRequest;
import com.hubbox.demo.dto.response.PinResponse;
import com.hubbox.demo.entities.PinEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PinMapper extends BaseMapper<PinCreateRequest, PinUpdateRequest, PinEntity, PinResponse> {
    PinMapper INSTANCE = Mappers.getMapper(PinMapper.class);
}
