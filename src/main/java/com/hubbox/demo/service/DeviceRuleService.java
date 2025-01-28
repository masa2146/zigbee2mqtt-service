package com.hubbox.demo.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.DeviceCriteria;
import com.hubbox.demo.dto.DeviceDataSnapshot;
import com.hubbox.demo.dto.RuleAction;
import com.hubbox.demo.dto.RuleCondition;
import com.hubbox.demo.dto.request.CreateDeviceRuleRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.request.UpdateDeviceRuleRequest;
import com.hubbox.demo.dto.response.DeviceRuleResponse;
import com.hubbox.demo.entities.DeviceRuleEntity;
import com.hubbox.demo.exceptions.BaseRuntimeException;
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
    private final Map<String, DeviceDataSnapshot> lastDeviceData = new ConcurrentHashMap<>();
    private final DeviceRuleRepository ruleRepository;
    private final DeviceCommandService deviceCommandService;
    private final DeviceRuleMapper mapper;

    @Inject
    public DeviceRuleService(DeviceRuleRepository ruleRepository,
                             DeviceCommandService deviceCommandService,
                             DeviceRuleMapper mapper) {
        this.ruleRepository = ruleRepository;
        this.deviceCommandService = deviceCommandService;
        this.mapper = mapper;
    }

    public DeviceRuleResponse createRule(CreateDeviceRuleRequest request) {
        try {
            DeviceRuleEntity rule = mapper.toEntity(request);
            Long id = ruleRepository.create(rule);
            rule.setId(id);
            return mapper.toResponse(rule);
        } catch (SQLException e) {
            log.error("Failed to create rule", e);
            throw new BaseRuntimeException("Failed to create rule", e);
        }
    }

    public DeviceRuleResponse getRule(Long id) {
        try {
            DeviceRuleEntity ruleEntity = findRuleById(id);
            return mapper.toResponse(ruleEntity);
        } catch (SQLException e) {
            log.error("Failed to get rule", e);
            throw new BaseRuntimeException("Failed to get rule", e);
        }
    }

    public List<DeviceRuleResponse> getAllRules() {
        try {
            return ruleRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
        } catch (SQLException e) {
            log.error("Failed to get rules", e);
            throw new BaseRuntimeException("Failed to get rules", e);
        }
    }

    public DeviceRuleResponse updateRule(Long id, UpdateDeviceRuleRequest request) {
        try {
            DeviceRuleEntity existingRule = findRuleById(id);
            mapper.updateEntityFromRequest(request, existingRule);
            ruleRepository.update(id, existingRule);

            return mapper.toResponse(existingRule);
        } catch (SQLException e) {
            log.error("Failed to update rule", e);
            throw new BaseRuntimeException("Failed to update rule", e);
        }
    }

    public void deleteRule(Long id) {
        try {
            findRuleById(id);
            ruleRepository.delete(id);
        } catch (SQLException e) {
            log.error("Failed to delete rule", e);
            throw new BaseRuntimeException("Failed to delete rule", e);
        }
    }

    public void processDeviceUpdate(String deviceId, Map<String, Object> deviceData) {
        try {
            // Güncel veriyi kaydet
            DeviceDataSnapshot currentSnapshot = new DeviceDataSnapshot(
                deviceId,
                deviceData,
                System.currentTimeMillis()
            );
            lastDeviceData.put(deviceId, currentSnapshot);

            List<DeviceRuleEntity> activeRules = ruleRepository.findAll();

            activeRules.stream()
                .filter(this::isComplexRuleTriggered)
                .forEach(rule -> {
                    log.debug("Complex Rule triggered: {}", rule.getId());
                    executeRuleAction(rule.getAction());
                });
        } catch (Exception e) {
            log.error("Error processing device update", e);
        }
    }

    private boolean isComplexRuleTriggered(DeviceRuleEntity rule) {
        RuleCondition condition = rule.getCondition();

        // Sıralı cihaz listesini kontrol et
        List<String> requiredDevices = condition.requiredDeviceSequence();

        // Tüm gerekli cihazların verisi var mı?
        boolean allDevicesPresent = requiredDevices.stream()
            .allMatch(lastDeviceData::containsKey);

        if (!allDevicesPresent) {
            return false;
        }

        // Zaman farkını kontrol et
        List<DeviceDataSnapshot> relevantSnapshots = requiredDevices.stream().map(lastDeviceData::get).toList();

        // İlk ve son snapshot arasındaki zaman farkını hesapla
        long timeDifference = relevantSnapshots.get(relevantSnapshots.size() - 1).timestamp()
            - relevantSnapshots.get(0).timestamp();

        if (timeDifference > condition.maxTimeDifferenceMs()) {
            return false;
        }

        // Tüm kriterleri kontrol et
        return relevantSnapshots.stream()
            .allMatch(snapshot ->
                evaluateDeviceCriteria(snapshot, condition.criteria())
            );
    }

    private boolean evaluateDeviceCriteria(
        DeviceDataSnapshot snapshot,
        List<DeviceCriteria> criteriaList
    ) {
        return criteriaList.stream()
            .filter(c -> c.deviceId().equals(snapshot.deviceId()))
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
            log.debug("Executing rule action: target={}, command={}",
                action.targetDeviceId(), action.commandName());
            SendDeviceCommandRequest commandRequest = mapper.toDeviceCommandRequest(action);
            deviceCommandService.executeCommand(commandRequest);
        } catch (Exception e) {
            log.error("Error executing rule action for device: {}", action.targetDeviceId(), e);
        }
    }

    private DeviceRuleEntity findRuleById(Long id) throws SQLException {
        return ruleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
    }

    @Override
    public void onDeviceDataReceived(String deviceId, Map<String, Object> data) {
        processDeviceUpdate(deviceId, data);
    }
}
