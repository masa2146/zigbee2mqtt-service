package com.hubbox.demo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubbox.demo.config.ConfigurationLoaderManager;
import com.hubbox.demo.dependecy.AppModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Zigbee2MattApplication {

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new AppModule());
            Server server = injector.getInstance(Server.class);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down server...");
                server.stop();
            }));
        } catch (Exception e) {
            log.error("Failed to start application", e);
            System.exit(1);
        }
    }
}
