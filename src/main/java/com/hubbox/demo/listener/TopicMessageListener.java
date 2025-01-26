package com.hubbox.demo.listener;

public interface TopicMessageListener {

    void onMessage(String topic, String message);
}
