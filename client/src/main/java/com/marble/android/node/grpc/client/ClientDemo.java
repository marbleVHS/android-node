package com.marble.android.node.grpc.client;

import com.marble.android.node.grpc.NodeServiceGrpc;
import com.marble.android.node.grpc.NodeServiceGrpc.NodeServiceStub;
import io.grpc.ManagedChannelBuilder;

import java.nio.file.Path;

public class ClientDemo {

    private static final String SERVER_HOST = "31.129.48.71";
    private static final int SERVER_PORT = 9090;


    public static void main(String[] args) {

        var channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext()
                .build();

        NodeServiceStub nodeServiceStub = NodeServiceGrpc.newStub(channel);

        Client client = new Client(nodeServiceStub);

        Path file = Path.of("/Users/vladislav.vorobev/Public/app-debug.apk");
        client.uploadFile(file);
    }

}
