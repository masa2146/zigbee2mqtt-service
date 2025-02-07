package com.hubbox.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.DeviceCriteria;
import com.hubbox.demo.dto.DeviceDataSnapshot;
import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;
import com.hubbox.demo.dto.request.ActivateRequest;
import com.hubbox.demo.dto.request.DeviceRuleCreateRequest;
import com.hubbox.demo.dto.request.DeviceRuleUpdateRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.response.DeviceRuleResponse;
import com.hubbox.demo.entities.DeviceRuleEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.RecordNotFoundException;
import com.hubbox.demo.listener.SensorEventListener;
import com.hubbox.demo.mapper.DeviceRuleMapper;
import com.hubbox.demo.repository.DeviceRuleRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceRuleService implements SensorEventListener {
    private static final String ALL_RULES_CACHE_KEY = "all";
    private final Map<String, DeviceDataSnapshot> lastDeviceData = new ConcurrentHashMap<>();
    private final DeviceRuleRepository ruleRepository;
    private final DeviceCommandService deviceCommandService;
    private final PinService pinService;
    private final DeviceRuleMapper mapper;
    private final Cache<String, List<DeviceRuleEntity>> ruleCache;

    @Inject
    public DeviceRuleService(DeviceRuleRepository ruleRepository,
                             DeviceCommandService deviceCommandService, PinService pinService,
                             DeviceRuleMapper mapper, Cache<String, List<DeviceRuleEntity>> ruleCache) {
        this.ruleRepository = ruleRepository;
        this.deviceCommandService = deviceCommandService;
        this.pinService = pinService;
        this.mapper = mapper;
        this.ruleCache = ruleCache;
    }

    public DeviceRuleResponse createRule(DeviceRuleCreateRequest request) {
        try {
            validateMaxDifference(request);
            DeviceRuleEntity rule = mapper.toEntity(request);
            rule.setEnabled(false);
            Long id = ruleRepository.create(rule);
            rule.setId(id);
            return mapper.toResponse(rule);
        } catch (SQLException e) {
            log.error("Failed newName create rule", e);
            throw new BaseRuntimeException("Failed newName create rule", e);
        }
    }

    public DeviceRuleResponse getRule(Long id) {
        try {
            DeviceRuleEntity ruleEntity = findRuleById(id);
            return mapper.toResponse(ruleEntity);
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Failed newName get rule", e);
            throw new BaseRuntimeException("Failed newName get rule", e);
        }
    }

    public List<DeviceRuleResponse> getAllRules() {
        return getAllRulesWithCache().stream().map(mapper::toResponse).toList();
    }

    public DeviceRuleResponse updateRule(Long id, DeviceRuleUpdateRequest request) {
        try {
            DeviceRuleEntity existingRule = findRuleById(id);
            mapper.updateEntityFromRequest(request, existingRule);
            ruleRepository.update(id, existingRule);

            return mapper.toResponse(existingRule);
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Failed newName update rule", e);
            throw new BaseRuntimeException("Failed newName update rule", e);
        }
    }

    public void deleteRule(Long id) {
        try {
            findRuleById(id);
            ruleRepository.delete(id);
        } catch (SQLException | RecordNotFoundException e) {
            log.error("Failed newName delete rule", e);
            throw new BaseRuntimeException("Failed newName delete rule", e);
        }
    }

    private List<DeviceRuleEntity> getAllRulesWithCache() {
        return ruleCache.get(ALL_RULES_CACHE_KEY, key -> {
            try {
                return ruleRepository.findAll();
            } catch (SQLException e) {
                log.error("Failed newName get rules", e);
                throw new BaseRuntimeException("Failed newName get rules", e);
            }
        });
    }

    public void activateRules(ActivateRequest request) throws SQLException {
        pinService.getPin(request.pinNumber());
        for (DeviceRuleEntity rule : getAllRulesWithCache()) {
            rule.setEnabled(request.activate());
            ruleRepository.update(rule.getId(), rule);
        }
    }

    public void processDeviceUpdate(String deviceName, Map<String, Object> deviceData) {
        try {
            List<DeviceRuleEntity> activeRules = getAllRulesWithCache().stream().filter(DeviceRuleEntity::getEnabled).toList();
            if (!isExistDeviceNameInCriteria(activeRules, deviceName)) {
                return;
            }

            // Güncel veriyi kaydet
            DeviceDataSnapshot currentSnapshot = new DeviceDataSnapshot(
                deviceName,
                deviceData,
                System.currentTimeMillis()
            );
            lastDeviceData.put(deviceName, currentSnapshot);


            activeRules.stream()
                .filter(this::isRuleTriggered)
                .forEach(rule -> {
                    log.debug("Rule triggered: {}", rule.getId());
                    executeRuleAction(rule.getAction());
                });
        } catch (Exception e) {
            log.error("Error processing device update", e);
        }
    }

    @Override
    public void onDeviceDataReceived(String deviceName, Map<String, Object> data) {
        processDeviceUpdate(deviceName, data);
    }

    private boolean isExistDeviceNameInCriteria(List<DeviceRuleEntity> activeRules, String deviceName) {
        return activeRules.stream()
            .anyMatch(deviceRuleEntity -> deviceRuleEntity.getCondition().requiredDeviceSequence().contains(deviceName));
    }

    private boolean isRuleTriggered(DeviceRuleEntity rule) {
        RuleCondition condition = rule.getCondition();

        // Sıralı cihaz listesini kontrol et
        List<String> requiredDevices = condition.requiredDeviceSequence();

        // Tüm gerekli cihazların verisi var mı?
        boolean allDevicesPresent = requiredDevices.stream().allMatch(lastDeviceData::containsKey);

        if (!allDevicesPresent) {
            return false;
        }

        // Zaman farkını kontrol et
        List<DeviceDataSnapshot> relevantSnapshots = requiredDevices.stream().map(lastDeviceData::get).toList();

        // İlk ve son snapshot arasındaki zaman farkını hesapla
        long timeDifference = relevantSnapshots.get(relevantSnapshots.size() - 1).timestamp() - relevantSnapshots.get(0).timestamp();

        if (condition.maxTimeDifferenceMs() != null && timeDifference > condition.maxTimeDifferenceMs()) {
            return false;
        }


        // Tüm kriterleri kontrol et
        return relevantSnapshots.stream().allMatch(snapshot -> evaluateDeviceCriteria(snapshot, condition.criteria())
        );
    }

    private boolean evaluateDeviceCriteria(DeviceDataSnapshot snapshot, List<DeviceCriteria> criteriaList) {
        return criteriaList.stream()
            .filter(c -> c.deviceName().equals(snapshot.deviceName()))
            .allMatch(criteria -> {
                Object actualValue = snapshot.data().get(criteria.field());
                if (actualValue == null) {
                    return false;
                }

                return criteria.operator().compare(actualValue, criteria.value());
            });
    }

    private void executeRuleAction(RuleAction action) {
        try {
            log.debug("Executing rule action: target={}, command={}", action.targetDeviceName(), action.commandName());
            SendDeviceCommandRequest commandRequest = mapper.toDeviceCommandRequest(action);
            deviceCommandService.executeCommand(commandRequest);
        } catch (Exception e) {
            log.error("Error executing rule action for device: {}", action.targetDeviceName(), e);
        }
    }

    private DeviceRuleEntity findRuleById(Long id) throws SQLException, RecordNotFoundException {
        return ruleRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("Rule not found: " + id));
    }

    private void validateMaxDifference(DeviceRuleCreateRequest request) {
        if (request.condition().maxTimeDifferenceMs() != null && request.condition().criteria().size() == 1) {
            throw new BaseRuntimeException("Time difference can only be used with multiple criteria");
        }

        if (request.condition().maxTimeDifferenceMs() == null && request.condition().criteria().size() > 1) {
            throw new BaseRuntimeException("Multiple criteria require a time difference");
        }
    }
}
