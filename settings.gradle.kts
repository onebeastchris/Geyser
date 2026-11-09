@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()

        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
    }
    includeBuild("build-logic")
}

rootProject.name = "geyser-parent"

// DRAFT wiring: Paletter (Bedrock palettes, item/block definitions, creative inventory) is consumed from a local
// checkout through a composite build until it is published. The path is relative to this repository, i.e. Paletter
// must be checked out next to Geyser as ../Paletter/Paletter-main. Replace with the published artifact
// (org.geysermc.paletter:paletter in libs.versions.toml) before merging.
includeBuild("../Paletter/Paletter-main") {
    dependencySubstitution {
        substitute(module("org.geysermc.paletter:paletter")).using(project(":paletter"))
    }
}

include(":ap")
include(":api")
include(":bungeecord")
include(":fabric")
include(":gametest")
include(":neoforge")
include(":mod")
include(":spigot")
include(":standalone")
include(":velocity")
include(":viaproxy")
include(":common")
include(":core")

// Specify project dirs
project(":bungeecord").projectDir = file("bootstrap/bungeecord")
project(":fabric").projectDir = file("bootstrap/mod/fabric")
project(":gametest").projectDir = file("bootstrap/mod/gametest")
project(":neoforge").projectDir = file("bootstrap/mod/neoforge")
project(":mod").projectDir = file("bootstrap/mod")
project(":spigot").projectDir = file("bootstrap/spigot")
project(":standalone").projectDir = file("bootstrap/standalone")
project(":velocity").projectDir = file("bootstrap/velocity")
project(":viaproxy").projectDir = file("bootstrap/viaproxy")

// Allow to download JVMs for toolchains
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}
