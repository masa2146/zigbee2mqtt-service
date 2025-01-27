package com.hubbox.demo.exceptions;

public class DependencyInitializationException extends BaseRuntimeException {
    public DependencyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
