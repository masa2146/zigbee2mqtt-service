package com.hubbox.demo.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hubbox.demo.config.MqttConfig;
import com.hubbox.demo.listener.TopicMessageListener;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MqttService implements AutoCloseable {

    private final Mqtt3AsyncClient client;
    private final MqttConfig mqttConfig;
    private final CompletableFuture<Boolean> connectionFuture;
    private final Map<String, TopicMessageListener> topicListeners = new ConcurrentHashMap<>();
    private boolean isSubscribed = false;

    @Inject
    public MqttService(Mqtt3AsyncClient client, MqttConfig mqttConfig) {
        this.client = client;
        this.mqttConfig = mqttConfig;
        this.connectionFuture = connect();
    }


    private CompletableFuture<Boolean> connect() {
        log.info("Connecting to MQTT broker...");
        return client.connectWith()
            .keepAlive(30)
            .cleanSession(true)
            .send()
            .thenApply(connAck -> {
                log.info("Connected to MQTT broker successfully");
                if (!isSubscribed) {
                    subscribeToMainTopic();
                }
                return true;
            })
            .exceptionally(throwable -> {
                log.error("Failed to connect to MQTT broker", throwable);
                return false;
            });
    }

    private void subscribeToMainTopic() {
        client.subscribeWith()
            .topicFilter(mqttConfig.topicFilter() + "#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(this::handleMessage)
            .send()
            .whenComplete((mqtt3SubAck, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to subscribe to main topic", throwable);
                    isSubscribed = false;
                    return;
                }
                log.info("Subscribed to main topic: {}", mqttConfig.topicFilter() + "#");
                isSubscribed = true;
            });
    }

    public void addTopicListener(String topic, TopicMessageListener listener) {
        topicListeners.put(mqttConfig.topicFilter() + topic, listener);
        log.debug("Added listener for topic: {}", topic);
    }

    public void removeTopicListener(String topic) {
        topicListeners.remove(mqttConfig.topicFilter() + topic);
        log.debug("Removed listener for topic: {}", topic);
    }

    private void handleMessage(Mqtt3Publish message) {
        String topic = message.getTopic().toString();
        String payload = new String(message.getPayloadAsBytes(), StandardCharsets.UTF_8);

        log.debug("Received message on topic: {}", topic);

        topicListeners.forEach((topicFilter, listener) -> {
            if (topic.matches(topicFilter.replace("#", ".*"))) {
                String topicName = topic.replace(mqttConfig.topicFilter(), "");
                listener.onMessage(topicName, payload);
            }
        });
    }


    public void sendCommand(String topicName, String command) {
        connectionFuture.thenCompose(connected -> {
            if (Boolean.FALSE.equals(connected)) {
                log.error("Cannot send command - not connected to broker");
                return CompletableFuture.failedFuture(
                    new IllegalStateException("Not connected to MQTT broker"));
            }

            String topic = mqttConfig.topicFilter() + topicName;
            log.debug("Preparing to send command. Topic: {}, Command: {}", topic, command);

            return client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(command.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        log.error("Command sending failed. Error: {}", throwable.getMessage(), throwable);
                    } else {
                        log.info("Command successfully sent.  Topic: {}", topic);
                    }
                })
                .thenApply(publish -> {
                    log.debug("Command processing completed");
                    return null;
                });
        });
    }

    @Override
    public void close() throws Exception {
        client.disconnect()
            .whenComplete((v, throwable) -> {
                if (throwable != null) {
                    log.error("Disconnect failed: {}", throwable.getMessage());
                    return;
                }
                log.info("Disconnected successfully");
            });
    }
}
