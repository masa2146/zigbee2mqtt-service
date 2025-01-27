package com.hubbox.demo.controller;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractController {
    private final String resourcePath;

    protected AbstractController(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    protected String buildPath(String... paths) {
        String[] allPaths = new String[paths.length + 1];
        allPaths[0] = resourcePath;
        System.arraycopy(paths, 0, allPaths, 1, paths.length);
        return String.join("/", allPaths);
    }


    public abstract void registerRoutes(Javalin app);
}
