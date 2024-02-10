package com.marble.android.node.controller;

import com.marble.android.node.environment.Paths;
import com.marble.android.node.grpc.DeleteFileRequest;
import com.marble.android.node.grpc.DeleteFileResponse;
import com.marble.android.node.grpc.NodeServiceGrpc;
import com.marble.android.node.grpc.UploadFileRequest;
import com.marble.android.node.grpc.UploadFileResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.marble.android.node.environment.Paths.FILES_DIRECTORY_PATH;

@GrpcService
public class NodeController extends NodeServiceGrpc.NodeServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeController.class);

    @Override
    public StreamObserver<UploadFileRequest> uploadFile(StreamObserver<UploadFileResponse> responseObserver) {
        return new StreamObserver<>() {

            private String uploadingFileName = null;

            @Override
            public void onNext(UploadFileRequest uploadFileRequest) {
                if (uploadFileRequest.getPartTypeCase() == UploadFileRequest.PartTypeCase.FILENAME) {
                    if (uploadingFileName == null) {
                        uploadingFileName = uploadFileRequest.getFileName();
                        try {
                            Files.deleteIfExists(Path.of(FILES_DIRECTORY_PATH, uploadingFileName));
                        } catch (IOException e) {
                            responseObserver.onError(
                                    Status.fromThrowable(e).asRuntimeException()
                            );
                            return;
                        }
                    }
                    if (!uploadFileRequest.getFileName().equals(uploadingFileName)) {
                        responseObserver.onError(
                                Status.fromThrowable(
                                        new Exception("You can't upload multiple files in a single session")
                                ).asRuntimeException()
                        );
                    }
                    return;
                }

                if (uploadingFileName == null) {
                    responseObserver.onError(
                            Status.fromThrowable(
                                    new IllegalArgumentException("Please send the file name first")
                            ).asRuntimeException()
                    );
                }

                try {
                    Path filesDirectory = Files.createDirectories(Path.of(FILES_DIRECTORY_PATH));
                    Path file = filesDirectory.resolve(uploadingFileName);
                    Files.write(
                            file,
                            uploadFileRequest.getFileChunk().toByteArray(),
                            StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE
                    );
                } catch (Exception e) {
                    LOGGER.error("Error during saving file chunk", e);
                    responseObserver.onError(
                            Status.fromThrowable(e).asRuntimeException()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Client error during uploading file {}", uploadingFileName, throwable);
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.info("Uploading file {} is finished", uploadingFileName);
                responseObserver.onNext(
                        UploadFileResponse
                                .newBuilder()
                                .setFileName(uploadingFileName)
                                .setMessage("File successfully uploaded")
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void deleteFile(DeleteFileRequest request, StreamObserver<DeleteFileResponse> responseObserver) {
        Path file = Path.of(FILES_DIRECTORY_PATH, request.getFileName());
        try {
            boolean deleted = Files.deleteIfExists(file);
            DeleteFileResponse.Builder builder = DeleteFileResponse
                    .newBuilder()
                    .setFileName(request.getFileName());
            if (deleted) {
                builder.setMessage("File was successfully deleted");
            } else {
                builder.setMessage("File wasn't found");
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(
                    Status.fromThrowable(e).asRuntimeException()
            );
        }
    }
}
