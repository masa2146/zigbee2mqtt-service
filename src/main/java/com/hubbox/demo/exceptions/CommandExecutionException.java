package com.hubbox.demo.exceptions;

public class CommandExecutionException extends BaseRuntimeException {
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
