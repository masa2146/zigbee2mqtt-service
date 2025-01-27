package com.hubbox.demo.exceptions;

public class CacheNotFoundException extends BaseRuntimeException {
    public CacheNotFoundException(String message) {
        super(message);
    }
}
