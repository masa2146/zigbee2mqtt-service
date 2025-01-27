package com.hubbox.demo.exceptions;

public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(Throwable e) {
        super(e);
    }

    public BaseRuntimeException(String message, Throwable e) {
        super(message, e);
    }

    public BaseRuntimeException(String message) {
        super(message);
    }
}
