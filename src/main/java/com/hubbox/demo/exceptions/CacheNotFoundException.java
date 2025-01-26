package com.hubbox.demo.exceptions;

public class CacheNotFoundException extends RuntimeException {
    public CacheNotFoundException(String message) {
        super(message);
    }
}
