rootProject.name = "android-node"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}



include("interface")
include("client")
include("node")
