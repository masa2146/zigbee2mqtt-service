package com.hubbox.demo.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.PinCreateRequest;
import com.hubbox.demo.dto.request.PinUpdateRequest;
import com.hubbox.demo.dto.response.PinResponse;
import com.hubbox.demo.entities.PinEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.RecordNotFoundException;
import com.hubbox.demo.mapper.PinMapper;
import com.hubbox.demo.repository.PinRepository;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PinService {

    private final PinRepository pinRepository;
    private final PinMapper pinMapper;

    @Inject
    public PinService(PinRepository pinRepository, PinMapper pinMapper) {
        this.pinRepository = pinRepository;
        this.pinMapper = pinMapper;
    }

    public PinResponse createPin(PinCreateRequest request) {
        try {
            List<PinResponse> allPins = getAllPins();
            if (allPins.size() > 1) {
                throw new BaseRuntimeException("Cannot create more than 2 pins");
            }
            PinEntity entity = pinMapper.toEntity(request);
            Long id = pinRepository.create(entity);
            entity.setId(id);
            return pinMapper.toResponse(entity);
        } catch (Exception e) {
            log.error("Failed newName create pin", e);
            throw new BaseRuntimeException("Failed newName create pin", e);
        }
    }

    public PinResponse getPin(String pinNumber) {
        try {
            PinEntity entity = findPinEntity(pinNumber);
            return pinMapper.toResponse(entity);
        } catch (Exception e) {
            log.error("Failed newName get pin", e);
            throw new BaseRuntimeException("Failed newName get pin", e);
        }
    }

    public PinResponse updatePin(String pinNumber, PinUpdateRequest request) {
        try {
            PinEntity entity = findPinEntity(pinNumber);
            pinMapper.updateEntityFromRequest(request, entity);
            pinRepository.update(entity.getId(), entity);
            return pinMapper.toResponse(entity);
        } catch (Exception e) {
            log.error("Failed newName update pin", e);
            throw new BaseRuntimeException("Failed newName update pin", e);
        }
    }

    public void deletePin(String pinNumber) {
        try {
            PinEntity entity = findPinEntity(pinNumber);
            pinRepository.delete(entity.getId());
        } catch (Exception e) {
            log.error("Failed newName delete pin", e);
            throw new BaseRuntimeException("Failed newName delete pin", e);
        }
    }

    public List<PinResponse> getAllPins() throws SQLException {
        return pinRepository.findAll().stream().map(pinMapper::toResponse).toList();
    }

    private PinEntity findPinEntity(String pinNumber) throws SQLException, RecordNotFoundException {
        return pinRepository.findByPinNumber(pinNumber).orElseThrow(() -> new RecordNotFoundException("Pin not found"));
    }
}
