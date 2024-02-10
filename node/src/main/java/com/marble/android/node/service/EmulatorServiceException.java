package com.marble.android.node.service;

import com.marble.android.node.grpc.EmulatorState;
import lombok.Getter;

@Getter
public class EmulatorServiceException extends RuntimeException {

    private final EmulatorState emulatorState;
    private final String message;

    public EmulatorServiceException(EmulatorState emulatorState, String message){
        this.emulatorState = emulatorState;
        this.message = message;
    }

}
