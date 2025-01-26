package com.hubbox.demo.exceptions;

public class DependencyShutdownException extends RuntimeException {
    public DependencyShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
