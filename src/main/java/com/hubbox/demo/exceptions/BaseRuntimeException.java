package com.hubbox.demo.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(JsonProcessingException e) {
        super(e);
    }

    public BaseRuntimeException(String message, Exception e) {
        super(message, e);
    }
}
