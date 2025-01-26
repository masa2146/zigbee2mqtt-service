package com.hubbox.demo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Zigbee2MattApplication {

    public static void main(String[] args) {
        // Create and start server
        Server server = new Server(8080);
        server.start();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.stop();
        }));
    }
}
