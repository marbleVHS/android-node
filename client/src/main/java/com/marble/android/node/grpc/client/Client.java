package com.marble.android.node.grpc.client;

import com.google.protobuf.ByteString;
import com.marble.android.node.grpc.NodeServiceGrpc.NodeServiceStub;
import com.marble.android.node.grpc.UploadFileRequest;
import com.marble.android.node.grpc.UploadFileResponse;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Client {

    private final NodeServiceStub nodeService;

    public Client(NodeServiceStub nodeService) {
        this.nodeService = nodeService;
    }

    public void uploadFile(Path file) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        StreamObserver<UploadFileRequest> uploadFileRequestStreamObserver = nodeService
                .withDeadlineAfter(20, TimeUnit.SECONDS)
                .uploadFile(
                        new StreamObserver<>() {
                            @Override
                            public void onNext(UploadFileResponse uploadFileResponse) {
                                System.out.println("uploadFileResponse:" + uploadFileResponse);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                System.out.println("Error while uploading file: " + throwable);
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("Finished uploading the file " + file.getFileName().toString());
                                countDownLatch.countDown();
                            }
                        }
                );

        UploadFileRequest fileNameRequest = UploadFileRequest
                .newBuilder()
                .setFileName(file.toFile().getName())
                .build();
        uploadFileRequestStreamObserver.onNext(fileNameRequest);

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (fileChannel.position() < fileChannel.size()) {
                if (countDownLatch.getCount() == 0) {
                    throw new IllegalStateException("Unexpected finish of file upload");
                }
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                UploadFileRequest uploadFileRequest = UploadFileRequest
                        .newBuilder()
                        .setFileChunk(ByteString.copyFrom(byteBuffer))
                        .build();
                uploadFileRequestStreamObserver.onNext(uploadFileRequest);
                byteBuffer.rewind();
            }
            uploadFileRequestStreamObserver.onCompleted();
            countDownLatch.await();
        } catch (IOException e) {
            uploadFileRequestStreamObserver.onError(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
