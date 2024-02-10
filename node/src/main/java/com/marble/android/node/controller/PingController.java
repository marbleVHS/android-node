package com.marble.android.node.controller;

import com.marble.android.node.grpc.PingServiceGrpc;
import com.marble.android.node.grpc.PongResponse;
import com.marble.android.node.grpc.Void;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PingController extends PingServiceGrpc.PingServiceImplBase {

    @Override
    public void ping(Void request, StreamObserver<PongResponse> responseObserver) {
        responseObserver.onNext(PongResponse.newBuilder().setMessage("pong").build());
        responseObserver.onCompleted();
    }

}
