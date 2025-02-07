package com.hubbox.demo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hubbox.demo.dependecy.AppModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Zigbee2MattApplication {

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new AppModule());
            Application application = injector.getInstance(Application.class);
            application.start();

        } catch (Exception e) {
            log.error("Failed newName start application", e);
            System.exit(1);
        }
    }
}
