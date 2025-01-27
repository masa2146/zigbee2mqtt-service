package com.hubbox.demo.exceptions;

public class DependencyShutdownException extends BaseRuntimeException {
    public DependencyShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
