package com.marble.android.node.controller;

import com.marble.android.node.environment.Paths;
import com.marble.android.node.grpc.EmulatorServiceGrpc;
import com.marble.android.node.grpc.EmulatorState;
import com.marble.android.node.grpc.InstallApkRequest;
import com.marble.android.node.grpc.InstallApkResponse;
import com.marble.android.node.grpc.StartEmulatorRequest;
import com.marble.android.node.grpc.StartEmulatorResponse;
import com.marble.android.node.grpc.StopEmulatorResponse;
import com.marble.android.node.grpc.Void;
import com.marble.android.node.service.AdbService;
import com.marble.android.node.service.EmulatorService;
import io.grpc.Status;
import io.grpc.netty.shaded.io.netty.handler.codec.Delimiters;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.File;
import java.util.concurrent.TimeUnit;

@GrpcService
public class EmulatorController extends EmulatorServiceGrpc.EmulatorServiceImplBase {

    private final EmulatorService emulatorService;
    private final AdbService adbService;

    public EmulatorController(
            EmulatorService emulatorService,
            AdbService adbService
    ) {
        this.emulatorService = emulatorService;
        this.adbService = adbService;
    }

    @Override
    public void startEmulator(StartEmulatorRequest request, StreamObserver<StartEmulatorResponse> responseObserver) {
        try {
            emulatorService.startEmulator(request.getWithAcceleration());
            responseObserver.onNext(
                    StartEmulatorResponse
                            .newBuilder()
                            .setEmulatorState(EmulatorState.STARTING)
                            .setMessage("Waiting emulator start")
                            .build()
            );
            long deadline = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3);
            while (System.currentTimeMillis() <= deadline) {
                Thread.sleep(10000L);
                if (emulatorService.isEmulatorStarted()) {
                    responseObserver.onNext(
                            StartEmulatorResponse
                                    .newBuilder()
                                    .setEmulatorState(EmulatorState.STARTED)
                                    .setMessage("Emulator successfully started")
                                    .build()
                    );
                    responseObserver.onCompleted();
                    break;
                }
                responseObserver.onNext(
                        StartEmulatorResponse
                                .newBuilder()
                                .setEmulatorState(EmulatorState.STARTING)
                                .setMessage("Waiting emulator start")
                                .build()
                );
            }
            if (emulatorService.isEmulatorStarted()) {
                responseObserver.onNext(
                        StartEmulatorResponse
                                .newBuilder()
                                .setEmulatorState(EmulatorState.STARTED)
                                .setMessage("Emulator successfully started")
                                .build()
                );
                responseObserver.onCompleted();
            } else {
                emulatorService.stopEmulator();
                responseObserver.onNext(
                        StartEmulatorResponse
                                .newBuilder()
                                .setEmulatorState(EmulatorState.FAILED)
                                .setMessage("Emulator start took too long time")
                                .build()
                );
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            responseObserver.onNext(
                    StartEmulatorResponse
                            .newBuilder()
                            .setEmulatorState(EmulatorState.FAILED)
                            .setMessage(e.getMessage())
                            .build()
            );
            responseObserver.onCompleted();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stopEmulator(Void request, StreamObserver<StopEmulatorResponse> responseObserver) {
        try {
            emulatorService.stopEmulator();
            responseObserver.onNext(
                    StopEmulatorResponse
                            .newBuilder()
                            .setEmulatorState(EmulatorState.STOPPED)
                            .setMessage("Emulator stopped successfully")
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(
                    StopEmulatorResponse
                            .newBuilder()
                            .setEmulatorState(EmulatorState.FAILED)
                            .setMessage(e.getMessage())
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void installApk(InstallApkRequest request, StreamObserver<InstallApkResponse> responseObserver) {
        String apkName = request.getApkName();
        if (!apkName.endsWith(".apk")) {
            responseObserver.onError(
                    Status.fromThrowable(
                            new IllegalArgumentException("Apk file must have .apk extension")
                    ).asRuntimeException()
            );
            return;
        }
        File file = new File(Paths.FILES_DIRECTORY_PATH + "/" + apkName);
        if (!file.exists()) {
            responseObserver.onError(
                    Status.fromThrowable(
                            new IllegalArgumentException("Apk file wasn't found on a node, you might want to upload it first")
                    ).asRuntimeException()
            );
            return;
        }
        try {
            adbService.executeAdbCommand("install " + Paths.FILES_DIRECTORY_PATH + "/" + apkName);
            responseObserver.onNext(
                    InstallApkResponse
                            .newBuilder()
                            .setApkName(apkName)
                            .setMessage("Apk was successfully installed")
                            .build()
            );
            responseObserver.onCompleted();
        }
        catch (Exception e ) {
            responseObserver.onError(
                    Status.fromThrowable(e).asRuntimeException()
            );
        }

    }
}
