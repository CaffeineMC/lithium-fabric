rootProject.name = "lithium"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}
includeBuild("components/mixin-config-plugin")

include("common")
include("fabric")
include("neoforge")
