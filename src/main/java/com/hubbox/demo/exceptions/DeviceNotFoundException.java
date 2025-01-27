package com.hubbox.demo.exceptions;

public class DeviceNotFoundException extends RecordNotFoundException {
    public DeviceNotFoundException(String message) {
        super(message);
    }
}
