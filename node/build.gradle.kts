plugins {
    java
    alias(libs.plugins.springboot)
    alias(libs.plugins.springDependency)
    alias(libs.plugins.jib)
}

group = "com.marble.android"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

jib {

    from {
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
        image = "marblevhs/liberica-latest-android:1.0.0"
    }

    container {
        ports = listOf("9090")
        environment = mapOf(
            "ANDROID_AVD_HOME" to "/.android/avd",
            "ANDROID_SDK_ROOT" to "/androidSdk",
            "ANDROID_HOME" to "/androidSdk",
            "ANDROID_SDK_HOME" to "/androidSdk",
        )
    }

    to {
        image = "marblevhs/android-node:${version}"
        auth {
            this.username = "marblevhs"
            this.password = "*********"
        }
    }

    extraDirectories {
        permissions = mapOf(
            "/androidSdk/emulator/emulator" to "755",
            "/androidSdk/emulator/qemu/linux-x86_64/qemu-system-x86_64-headless" to "755",
        )
    }

}

dependencies {
    implementation(libs.thymeleafStarter)
    implementation(libs.webStarter)
    implementation(libs.grpcStarter)
    implementation(projects.`interface`)
    compileOnly(libs.lombok)
    developmentOnly(libs.devTools)
    annotationProcessor(libs.lombok)
    testImplementation(libs.testStarter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
