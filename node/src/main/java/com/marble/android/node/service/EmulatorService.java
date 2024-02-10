package com.marble.android.node.service;

public interface EmulatorService {

    void startEmulator(boolean withAccel);

    void stopEmulator();

    boolean isEmulatorStarted();
}
