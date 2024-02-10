package com.marble.android.node.service;

import com.marble.android.node.environment.Paths;
import com.marble.android.node.environment.runtime.CommandExecutor;
import com.marble.android.node.grpc.EmulatorState;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
public class EmulatorServiceImpl implements EmulatorService {
    private final CommandExecutor commandExecutor;

    private EmulatorState emulatorState = EmulatorState.STOPPED;
    private Process emulatorProcess = null;

    public EmulatorServiceImpl(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void startEmulator(boolean withAccel) {
        if (emulatorState == EmulatorState.STARTED) {
            throw new EmulatorServiceException(EmulatorState.STARTED, "Emulator is already started");
        }
        if (emulatorState == EmulatorState.STARTING) {
            throw new EmulatorServiceException(EmulatorState.STARTING, "Emulator is starting, wait until it started completely");
        }
        if (withAccel) {
            emulatorProcess = commandExecutor.startProcess(Paths.EMULATOR_PATH + " @Pixel2 -no-window -netfast -skip-adb-auth -accel on -gpu swiftshader_indirect -qemu -enable-kvm");
        } else {
            emulatorProcess = commandExecutor.startProcess(Paths.EMULATOR_PATH + " @Pixel2 -no-window -netfast -skip-adb-auth -accel off -gpu swiftshader_indirect");
        }
        emulatorState = EmulatorState.STARTING;
    }

    @Override
    public void stopEmulator() {
        if (emulatorState == EmulatorState.STOPPED || emulatorProcess == null) {
            throw new EmulatorServiceException(EmulatorState.STOPPED, "Emulator is already stopped");
        }
        emulatorProcess.destroy();
        emulatorProcess = null;
        emulatorState = EmulatorState.STOPPED;
    }

    @Override
    public boolean isEmulatorStarted() {
        boolean started = commandExecutor.executeCommand(Paths.ADB_PATH + " shell getprop init.svc.bootanim", true).contains("stopped");
        if (started) {
            emulatorState = EmulatorState.STARTED;
        }
        return started;
    }
}
