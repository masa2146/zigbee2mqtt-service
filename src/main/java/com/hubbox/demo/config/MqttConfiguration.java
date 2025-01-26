package com.hubbox.demo.config;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MqttConfiguration {

    private final MqttConfig mqttConfig;


    public Mqtt3AsyncClient mqttClient() {
        return MqttClient.builder()
            .useMqttVersion3()
            .identifier(mqttConfig.id())
            .serverHost(mqttConfig.host())
            .serverPort(mqttConfig.port())
            .buildAsync();
    }
}
