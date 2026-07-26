import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("geyser.isolated-platform-conventions")
    id("geyser.modrinth-uploading-conventions")
    alias(libs.plugins.runpaper)
}

// The Adventure modules loaded at runtime when the server does not provide a usable Adventure version
// (Spigot, or Paper bundling an older major version). Must cover extensions.kt's adventureRuntimeModules,
// which the base jar excludes; transitives (key, nbt, json, commons, option, ...) are resolved automatically.
val adventureBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    compileOnly(libs.folia.api)

    adventureBundle(libs.bundles.adventure)
    adventureBundle(libs.adventure.text.minimessage)
}

// Merged into a single jar so Repository.BUNDLED can load it as one library
val adventureJar = tasks.register<ShadowJar>("adventureJar") {
    description = "Merges the runtime-loaded Adventure library into a single jar"
    configurations = listOf(adventureBundle)
    archiveFileName = "adventure.jar"
    destinationDirectory = layout.buildDirectory.dir("adventureBundle")
    mergeServiceFiles()
    exclude("module-info.class", "META-INF/versions/**/module-info.class")
    dependencies {
        // Annotations, and gson/jspecify - already provided by the server or the isolated loader
        exclude(dependency("org.jetbrains:.*"))
        exclude(dependency("org.checkerframework:.*"))
        exclude(dependency("com.google.*:.*"))
        exclude(dependency("org.jspecify:.*"))
        // Stays in the base jar - Paper doesn't ship it, so it is needed in all modes
        exclude(dependency("net.kyori:adventure-nbt:.*"))
    }
}

tasks {
    jar {
        manifest.attributes["Main-Class"] = "org.geysermc.geyser.platform.spigot.GeyserSpigotMain"
    }

    shadowJar {
        archiveBaseName.set("Geyser-Spigot")

        from(adventureJar) {
            into("bundled/")
        }
    }

    runServer {
        minecraftVersion(libs.versions.runpaperversion.get())
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}

modrinth {
    uploadFile.set(tasks.getByPath("shadowJar"))
    gameVersions.addAll("1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4",
        "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2")
    loaders.addAll("spigot", "paper")
}
