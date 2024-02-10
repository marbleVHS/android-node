plugins {
    java
}

group = "com.marble.android"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(projects.`interface`)
    implementation(libs.grpcNetty)
}