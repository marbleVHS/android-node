import com.google.protobuf.gradle.id

plugins {
    `java-library`
    idea
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.grpcProtobuf)
    api(libs.grpcStub)
    compileOnly(libs.jakartaAnnotationApi)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}


idea {
    module {
        sourceDirs.plusAssign(file("src/generated/main/java"))
        sourceDirs.plusAssign(file("src/generated/main/grpc"))
        generatedSourceDirs.plusAssign(file("src/generated/main/java"))
        generatedSourceDirs.plusAssign(file("src/generated/main/grpc"))
    }
}
