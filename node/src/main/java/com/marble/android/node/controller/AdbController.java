package com.marble.android.node.controller;

import com.marble.android.node.grpc.AdbServiceGrpc;
import com.marble.android.node.grpc.ExecuteAdbCommandRequest;
import com.marble.android.node.grpc.ExecuteAdbCommandResponse;
import com.marble.android.node.service.AdbService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AdbController extends AdbServiceGrpc.AdbServiceImplBase {

    private final AdbService adbService;

    public AdbController(AdbService adbService) {
        this.adbService = adbService;
    }

    @Override
    public void executeAdbCommand(ExecuteAdbCommandRequest request, StreamObserver<ExecuteAdbCommandResponse> responseObserver) {
        try {
            String output = adbService.executeAdbCommand(request.getCommand());
            responseObserver.onNext(
                    ExecuteAdbCommandResponse
                            .newBuilder()
                            .setOutput(output)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(
                    ExecuteAdbCommandResponse
                            .newBuilder()
                            .setOutput("ERROR: " + e.getMessage())
                            .build()
            );
            responseObserver.onCompleted();
        }
    }
}
